package com.example.stealmacro.gui;

import com.example.stealmacro.Keys;
import com.example.stealmacro.Model;
import com.example.stealmacro.Model.StepType;
import com.example.stealmacro.Store;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StepsScreen extends BaseScreen {
    private ListView list;

    public StepsScreen() { super("Шаги"); }
    @Override protected int tab() { return 2; }

    private Model.Macro macro() {
        List<Model.Macro> ms = Store.data.macros;
        return ms.isEmpty() ? null : ms.get(Mth.clamp(selMacro, 0, ms.size() - 1));
    }

    private List<String> rows(Model.Macro m) {
        List<String> r = new ArrayList<>();
        for (int i = 0; i < m.steps.size(); i++) r.add((i + 1) + ". " + describe(m.steps.get(i)));
        return r;
    }

    @Override protected void init() {
        super.init();
        Model.Macro m = macro();
        if (m == null) {
            labels.add(new Object[]{"Нет макросов - создай на вкладке «Макросы».", 10, 40});
            return;
        }
        selMacro = Store.data.macros.indexOf(m);
        selStep = m.steps.isEmpty() ? -1 : Mth.clamp(selStep, 0, m.steps.size() - 1);
        list = makeList(selStep, 78);
        list.rows = rows(m);

        int half = (listW - 4) / 2;
        btn("▲ Выше", 10, height - 72, half, () -> move(m, -1));
        btn("▼ Ниже", 10 + half + 4, height - 72, half, () -> move(m, 1));
        listButtons(() -> {
            Model.Step s = Model.step(StepType.WAIT, x -> x.ms = 300);
            int at = selStep < 0 ? m.steps.size() : selStep + 1;
            m.steps.add(at, s); selStep = at; rebuildWidgets();
        }, () -> {
            if (selStep < 0) return;
            m.steps.add(selStep + 1, Store.copy(m.steps.get(selStep), Model.Step.class));
            selStep++; rebuildWidgets();
        }, () -> {
            if (selStep < 0) return;
            m.steps.remove(selStep); selStep = Math.max(0, selStep - 1); rebuildWidgets();
        });

        List<String> names = new ArrayList<>();
        for (Model.Macro x : Store.data.macros) names.add(x.name);
        choice("Макрос", names, selMacro, i -> { selMacro = i; selStep = 0; rebuildWidgets(); });
        if (selStep >= 0) form(m.steps.get(selStep));
    }

    private void move(Model.Macro m, int d) {
        int to = selStep + d;
        if (selStep < 0 || to < 0 || to >= m.steps.size()) return;
        Collections.swap(m.steps, selStep, to);
        selStep = to; rebuildWidgets();
    }

    // ---------- форма шага ----------
    private List<String> abilityNames() {
        List<String> n = new ArrayList<>();
        for (Model.Ability a : Store.data.abilities) n.add(a.name);
        return n;
    }

    private void abilityPick(Model.Step s) {
        List<String> n = abilityNames();
        choice("Способность", n, n.indexOf(s.ability), i -> s.ability = n.get(i));
    }

    private void coords(Model.Step s, boolean showHit) {
        number("X", s.x, v -> s.x = v);
        number("Y", s.y, v -> s.y = v);
        number("Z", s.z, v -> s.z = v);
        formBtn("Скопировать мои координаты", () -> {
            LocalPlayer p = minecraft.player;
            if (p == null) return;
            s.x = r2(p.getX()); s.y = r2(p.getY()); s.z = r2(p.getZ());
            rebuildWidgets();
        });
        if (showHit) formBtn("Скопировать точку под прицелом", () -> {
            HitResult h = minecraft.hitResult;
            if (h == null || h.getType() == HitResult.Type.MISS) return;
            Vec3 v = h.getLocation();
            s.x = r2(v.x); s.y = r2(v.y); s.z = r2(v.z);
            rebuildWidgets();
        });
    }

    private void condFields(Model.Step s) {
        cycle("Условие", s.cond, c -> { s.cond = c; rebuildWidgets(); });
        switch (s.cond) {
            case AIMED_AT_TARGET -> {
                cycle("Цель", s.target, t -> s.target = t);
                number("Дальность, блоков", s.num, v -> s.num = v);
            }
            case TARGET_WITHIN, TARGET_BEYOND -> {
                cycle("Цель", s.target, t -> s.target = t);
                number("Дистанция, блоков", s.num, v -> s.num = v);
            }
            case COOLDOWN_READY, COOLDOWN_ACTIVE -> abilityPick(s);
            case HEALTH_BELOW -> number("HP (сердце = 2)", s.num, v -> s.num = v);
            case NEAR_POS -> { coords(s, false); number("Радиус, блоков", s.num, v -> s.num = v); }
            case CHAT_CONTAINS -> text("Текст", s.text, v -> s.text = v);
        }
    }

    private void form(Model.Step s) {
        cycle("Тип шага", s.type, t -> {
            s.type = t;
            if (t == StepType.WAIT_UNTIL) { s.onFail = Model.Fail.SKIP; if (s.ms < 500) s.ms = 3000; }
            rebuildWidgets();
        });
        switch (s.type) {
            case USE_ABILITY -> {
                abilityPick(s);
                choice("Если КД не готов", List.of("ждать", "пропустить", "стоп макроса"), s.onFail.ordinal(), i -> s.onFail = Model.Fail.values()[i]);
            }
            case SELECT_SLOT -> number("Слот 1-9", s.slot, v -> s.slot = v.intValue());
            case PRESS_KEY -> {
                keyButton("Клавиша / клик", s.key, k -> s.key = k);
                number("Удержание, мс", s.ms, v -> s.ms = v.intValue());
            }
            case KEY_DOWN, KEY_UP -> keyButton("Клавиша / клик", s.key, k -> s.key = k);
            case WAIT -> number("Ждать, мс", s.ms, v -> s.ms = v.intValue());
            case ROTATE -> {
                choice("Режим", List.of("Абсолютный", "Относительный (+Δ)"), s.relative ? 1 : 0, i -> s.relative = i == 1);
                number("Yaw (влево/вправо)", s.yaw, v -> s.yaw = v.floatValue());
                number("Pitch (вверх/вниз)", s.pitch, v -> s.pitch = v.floatValue());
                number("Время, мс", s.ms, v -> s.ms = v.intValue());
                formBtn("Скопировать мою ротацию", () -> {
                    LocalPlayer p = minecraft.player;
                    if (p == null) return;
                    s.yaw = (float) r2(Mth.wrapDegrees(p.getYRot())); s.pitch = (float) r2(p.getXRot()); s.relative = false;
                    rebuildWidgets();
                });
            }
            case AIM -> {
                cycle("Цель", s.target, t -> s.target = t);
                number("Дальность, блоков", s.num, v -> s.num = v);
                number("Время наводки, мс", s.ms, v -> s.ms = v.intValue());
            }
            case LOOK_AT_POS -> {
                coords(s, true);
                number("Время, мс", s.ms, v -> s.ms = v.intValue());
            }
            case WAIT_UNTIL -> {
                condFields(s);
                number("Таймаут, мс (0=∞)", s.ms, v -> s.ms = v.intValue());
                choice("По таймауту", List.of("идти дальше", "стоп макроса"), s.onFail == Model.Fail.STOP ? 1 : 0,
                        i -> s.onFail = i == 1 ? Model.Fail.STOP : Model.Fail.SKIP);
            }
            case IF -> {
                condFields(s);
                number("ДА → шаг №", s.jumpTrue, v -> s.jumpTrue = v.intValue());
                number("НЕТ → шаг №", s.jumpFalse, v -> s.jumpFalse = v.intValue());
                note("Шаг №: 0 = следующий, -1 = остановить макрос.");
            }
            case STOP -> {}
        }
    }

    // ---------- описание ----------
    private static String condText(Model.Step s) {
        return switch (s.cond) {
            case AIMED_AT_TARGET -> "прицел на цели (" + s.target.label() + ")";
            case TARGET_WITHIN -> "цель ближе " + fmt(s.num);
            case TARGET_BEYOND -> "цель дальше " + fmt(s.num) + " / нет";
            case COOLDOWN_READY -> "КД готов: " + s.ability;
            case COOLDOWN_ACTIVE -> "КД идёт: " + s.ability;
            case HEALTH_BELOW -> "HP ≤ " + fmt(s.num);
            case NEAR_POS -> "рядом с точкой";
            case CHAT_CONTAINS -> "в чате «" + s.text + "»";
        };
    }

    static String describe(Model.Step s) {
        return switch (s.type) {
            case USE_ABILITY -> "Способность: " + s.ability;
            case SELECT_SLOT -> "Слот " + s.slot;
            case PRESS_KEY -> "Нажать " + Keys.display(s.key) + " (" + s.ms + " мс)";
            case KEY_DOWN -> "Зажать " + Keys.display(s.key);
            case KEY_UP -> "Отпустить " + Keys.display(s.key);
            case WAIT -> "Ждать " + s.ms + " мс";
            case ROTATE -> (s.relative ? "Поворот Δ " : "Поворот ") + fmt(s.yaw) + " / " + fmt(s.pitch);
            case AIM -> "Навестись: " + s.target.label();
            case LOOK_AT_POS -> "Смотреть на " + fmt(s.x) + " " + fmt(s.y) + " " + fmt(s.z);
            case WAIT_UNTIL -> "Ждать: " + condText(s);
            case IF -> "Если " + condText(s) + " → " + s.jumpTrue + " / " + s.jumpFalse;
            case STOP -> "Стоп макроса";
        };
    }

    @Override protected void renderBody(GuiGraphics g, int mx, int my) {
        if (list == null) return;
        Model.Macro m = macro();
        if (m != null) list.rows = rows(m);
        list.render(g, font, mx, my);
    }
    @Override protected boolean clickBody(double mx, double my) {
        if (list != null && list.click(mx, my)) { selStep = list.sel; rebuildWidgets(); return true; }
        return false;
    }
    @Override protected boolean scrollBody(double mx, double my, double d) { return list != null && list.wheel(mx, my, d); }
}
