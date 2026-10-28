package com.example.stealmacro;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/** Движок макросов: каждый тик выполняет очередь "операций". */
public final class Runner {
    private Runner() {}

    private static Model.Macro current;
    private static int pc;
    private static BooleanSupplier op;
    private static final Deque<BooleanSupplier> queue = new ArrayDeque<>();
    private static final Set<String> held = new HashSet<>();
    private static final Random RNG = new Random();
    private static long macroStart, lastActionAt;

    public static Model.Macro current() { return current; }
    public static int pc() { return pc; }

    public static void start(Model.Macro m) {
        stop();
        if (m == null || m.steps.isEmpty()) return;
        current = m; pc = 0;
        macroStart = lastActionAt = System.currentTimeMillis();
    }

    public static void stop() {
        current = null; op = null; queue.clear();
        for (String k : held) Keys.set(k, false);
        held.clear();
    }

    public static void tick(Minecraft mc) {
        if (current == null) return;
        if (mc.player == null || mc.level == null) { stop(); return; }
        if (mc.screen != null) return;
        for (int guard = 0; guard < 100 && current != null; guard++) {
            if (op == null) op = queue.poll();
            if (op == null) {
                if (pc >= current.steps.size()) {
                    if (current.loop || current.mode == Model.RunMode.HOLD) { pc = 0; queue.add(delay(1)); continue; }
                    stop();
                    return;
                }
                exec(current.steps.get(pc++));
                continue;
            }
            if (!op.getAsBoolean()) return;
            op = null;
        }
    }

    // ---------- helpers ----------
    private static int ticks(int ms) { return Math.max(0, Math.round(ms / 50f)); }

    private static int rand(int ms) {
        int p = Store.data.settings.randomPercent;
        if (p <= 0 || ms <= 0) return ms;
        double f = 1 + (RNG.nextDouble() * 2 - 1) * p / 100.0;
        return (int) Math.max(0, ms * f);
    }

    private static BooleanSupplier delay(int ticks) {
        int[] c = {ticks};
        return () -> c[0]-- <= 0;
    }

    private static void setSlot(int n) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p != null && n >= 1 && n <= 9) p.getInventory().selected = n - 1;
    }

    private static void jump(int n) {
        if (n < 0) stop();
        else if (n > 0) pc = n - 1;
    }

    private static void press(String key, int holdMs) {
        queue.add(() -> { Keys.set(key, true); held.add(key); lastActionAt = System.currentTimeMillis(); return true; });
        queue.add(delay(Math.max(1, ticks(holdMs))));
        queue.add(() -> { Keys.set(key, false); held.remove(key); return true; });
    }

    private static BooleanSupplier look(Supplier<float[]> tgt, int ms) {
        int[] left = {Math.max(1, ticks(rand(ms)))};
        return () -> {
            LocalPlayer p = Minecraft.getInstance().player;
            float[] t = tgt.get();
            if (p == null || t == null) return true;
            int n = left[0];
            p.setYRot(p.getYRot() + Mth.wrapDegrees(t[0] - p.getYRot()) / n);
            p.setXRot(Mth.clamp(p.getXRot() + (t[1] - p.getXRot()) / n, -90f, 90f));
            return --left[0] <= 0;
        };
    }

    private static void useAbility(Model.Step s) {
        Model.Ability a = Cooldowns.find(s.ability);
        if (a == null) return;
        queue.add(() -> {
            if (!Cooldowns.ready(a)) {
                if (s.onFail == Model.Fail.SKIP) return true;
                if (s.onFail == Model.Fail.STOP) { stop(); return true; }
                return false; // WAIT
            }
            if (a.slot >= 1 && a.slot <= 9) {
                queue.add(() -> { setSlot(a.slot); return true; });
                queue.add(delay(ticks(Store.data.settings.slotDelayMs)));
            }
            queue.add(() -> { Cooldowns.start(a); return true; });
            press(a.key, rand(a.holdMs));
            return true;
        });
    }

    private static void exec(Model.Step s) {
        switch (s.type) {
            case USE_ABILITY -> useAbility(s);
            case SELECT_SLOT -> {
                queue.add(() -> { setSlot(s.slot); return true; });
                queue.add(delay(ticks(Store.data.settings.slotDelayMs)));
            }
            case PRESS_KEY -> press(s.key, rand(s.ms));
            case KEY_DOWN -> queue.add(() -> { Keys.set(s.key, true); held.add(s.key); lastActionAt = System.currentTimeMillis(); return true; });
            case KEY_UP -> queue.add(() -> { Keys.set(s.key, false); held.remove(s.key); return true; });
            case WAIT -> queue.add(delay(ticks(rand(s.ms))));
            case ROTATE -> {
                float[] t = new float[2];
                boolean[] init = {false};
                queue.add(look(() -> {
                    LocalPlayer p = Minecraft.getInstance().player;
                    if (!init[0]) {
                        init[0] = true;
                        t[0] = s.relative ? p.getYRot() + s.yaw : s.yaw;
                        t[1] = s.relative ? p.getXRot() + s.pitch : s.pitch;
                    }
                    return t;
                }, s.ms));
            }
            case AIM -> queue.add(look(() -> {
                Entity e = Targets.nearest(s.target, s.num);
                return e == null ? null : Targets.anglesTo(Targets.center(e));
            }, s.ms));
            case LOOK_AT_POS -> queue.add(look(() -> Targets.anglesTo(new net.minecraft.world.phys.Vec3(s.x, s.y, s.z)), s.ms));
            case WAIT_UNTIL -> {
                long[] t0 = {0};
                queue.add(() -> {
                    long now = System.currentTimeMillis();
                    if (t0[0] == 0) t0[0] = now;
                    if (Cond.eval(s, lastActionAt)) return true;
                    if (s.ms > 0 && now - t0[0] > s.ms) {
                        if (s.onFail == Model.Fail.STOP) stop();
                        return true;
                    }
                    return false;
                });
            }
            case IF -> queue.add(() -> { jump(Cond.eval(s, lastActionAt) ? s.jumpTrue : s.jumpFalse); return true; });
            case STOP -> queue.add(() -> { stop(); return true; });
        }
    }
                                                       }
