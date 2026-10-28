package com.example.stealmacro;

import com.example.stealmacro.gui.BaseScreen;
import com.example.stealmacro.gui.StepsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/** Запись "повтори за мной". */
public final class Recorder {
    private Recorder() {}

    public static boolean active;
    private static Model.Macro target;
    private static final List<Model.Step> steps = new ArrayList<>();
    private static final Set<String> down = new HashSet<>();
    private static long last;
    private static int lastSlot, startSlot;
    private static float recYaw, recPitch;
    private static boolean aimed;

    public static int count() { return steps.size(); }

    public static void start(Model.Macro m) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) return;
        target = m; steps.clear(); down.clear(); last = 0; aimed = false;
        lastSlot = startSlot = p.getInventory().selected;
        recYaw = p.getYRot(); recPitch = p.getXRot();
        active = true;
    }

    public static void cancel() { active = false; steps.clear(); down.clear(); }

    public static void tick(Minecraft mc) {
        if (active && mc.player != null && mc.screen == null) pollSlot(mc.player);
    }

    private static void add(Model.Step s) {
        long now = System.currentTimeMillis();
        if (last > 0 && now - last >= 50) {
            int gap = (int) (now - last);
            steps.add(Model.step(Model.StepType.WAIT, w -> w.ms = gap));
        }
        steps.add(s);
        last = now;
    }

    private static void pollSlot(LocalPlayer p) {
        int s = p.getInventory().selected;
        if (s != lastSlot) addSlot(s + 1);
    }

    private static void addSlot(int slot1) {
        if (slot1 - 1 == lastSlot) return;
        lastSlot = slot1 - 1;
        add(Model.step(Model.StepType.SELECT_SLOT, s -> s.slot = slot1));
    }

    private static void captureCamera(LocalPlayer p) {
        Model.Settings st = Store.data.settings;
        if (st.camMode == Model.CamMode.TARGET) {
            Entity e = Targets.aimed(Model.TargetKind.NEAREST_ANY, st.recordRange);
            if (e != null) {
                if (!aimed) {
                    Model.TargetKind k = e instanceof Player ? Model.TargetKind.NEAREST_PLAYER : Model.TargetKind.NEAREST_MOB;
                    add(Model.step(Model.StepType.AIM, s -> { s.target = k; s.num = st.recordRange; s.ms = 100; }));
                    aimed = true;
                }
                recYaw = p.getYRot(); recPitch = p.getXRot();
                return;
            }
            aimed = false;
        }
        float dy = Math.abs(Mth.wrapDegrees(p.getYRot() - recYaw));
        float dp = Math.abs(p.getXRot() - recPitch);
        if (dy > 3 || dp > 3) {
            float yaw = Mth.wrapDegrees(p.getYRot()), pitch = p.getXRot();
            add(Model.step(Model.StepType.ROTATE, s -> { s.yaw = yaw; s.pitch = pitch; s.relative = false; s.ms = 100; }));
            recYaw = p.getYRot(); recPitch = p.getXRot();
        }
    }

    public static void onInput(String name, boolean press) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) return;
        if (name.equals(Keys.STOP.getKey().getName()) || name.equals(Keys.MENU.getKey().getName())) return;
        pollSlot(p);
        if (name.matches("key\\.keyboard\\.[1-9]")) {
            if (press) addSlot(name.charAt(name.length() - 1) - '0');
            return;
        }
        if (press) {
            if (!down.add(name)) return;
            captureCamera(p);
            add(Model.step(Model.StepType.KEY_DOWN, s -> s.key = name));
        } else {
            if (!down.remove(name)) return;
            add(Model.step(Model.StepType.KEY_UP, s -> s.key = name));
        }
    }

    public static void finish() {
        if (!active) return;
        active = false;
        for (String k : new ArrayList<>(down)) add(Model.step(Model.StepType.KEY_UP, s -> s.key = k));
        down.clear();
        List<Model.Step> out = match(collapse(new ArrayList<>(steps)));
        if (target != null) target.steps.addAll(out);
        Store.save();
        Minecraft mc = Minecraft.getInstance();
        BaseScreen.selMacro = Math.max(0, Store.data.macros.indexOf(target));
        BaseScreen.selStep = target == null || target.steps.isEmpty() ? -1 : target.steps.size() - 1;
        mc.setScreen(new StepsScreen());
        if (mc.player != null) mc.player.displayClientMessage(Component.literal("[StealMacro] записано шагов: " + out.size()), true);
    }

    /** KEY_DOWN + WAIT + KEY_UP -> PRESS_KEY */
    private static List<Model.Step> collapse(List<Model.Step> in) {
        List<Model.Step> out = new ArrayList<>();
        for (int i = 0; i < in.size(); i++) {
            Model.Step s = in.get(i);
            if (s.type == Model.StepType.KEY_DOWN) {
                if (i + 1 < in.size() && in.get(i + 1).type == Model.StepType.KEY_UP && in.get(i + 1).key.equals(s.key)) {
                    out.add(pressStep(s.key, 50)); i += 1; continue;
                }
                if (i + 2 < in.size() && in.get(i + 1).type == Model.StepType.WAIT
                        && in.get(i + 2).type == Model.StepType.KEY_UP && in.get(i + 2).key.equals(s.key)) {
                    out.add(pressStep(s.key, in.get(i + 1).ms)); i += 2; continue;
                }
            }
            out.add(s);
        }
        return out;
    }

    private static Model.Step pressStep(String key, int ms) {
        return Model.step(Model.StepType.PRESS_KEY, s -> { s.key = key; s.ms = ms; });
    }

    /** слот + клик, совпавшие со справочником, заменяем на "Использовать способность" */
    private static List<Model.Step> match(List<Model.Step> in) {
        List<Model.Step> out = new ArrayList<>();
        int slot = startSlot + 1;
        for (int i = 0; i < in.size(); i++) {
            Model.Step s = in.get(i);
            if (s.type == Model.StepType.SELECT_SLOT) {
                slot = s.slot;
                int j = i + 1;
                if (j < in.size() && in.get(j).type == Model.StepType.WAIT && in.get(j).ms <= 600) j++;
                if (j < in.size() && in.get(j).type == Model.StepType.PRESS_KEY) {
                    Model.Ability a = find(slot, in.get(j).key);
                    if (a != null) { out.add(use(a)); i = j; continue; }
                }
            } else if (s.type == Model.StepType.PRESS_KEY) {
                Model.Ability a = find(slot, s.key);
                if (a != null) { out.add(use(a)); continue; }
            }
            out.add(s);
        }
        return out;
    }

    private static Model.Step use(Model.Ability a) {
        return Model.step(Model.StepType.USE_ABILITY, s -> { s.ability = a.name; s.onFail = Model.Fail.WAIT; });
    }

    private static Model.Ability find(int slot, String key) {
        for (Model.Ability a : Store.data.abilities) if (key.equals(a.key) && a.slot == slot) return a;
        for (Model.Ability a : Store.data.abilities) if (key.equals(a.key) && a.slot == 0) return a;
        return null;
    }
}
