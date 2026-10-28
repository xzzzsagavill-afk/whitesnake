package com.example.stealmacro;

import java.util.ArrayList;
import java.util.List;

/** Пресет White Snake. Слоты 1-4 и их КД я прикинул сам: поправь в меню. */
public final class Defaults {
    private Defaults() {}

    static Model.Ability ab(String name, int slot, String key, double cd, String group) {
        Model.Ability a = new Model.Ability();
        a.name = name; a.slot = slot; a.key = key; a.cooldown = cd; a.group = group;
        return a;
    }

    public static List<Model.Ability> whiteSnake() {
        List<Model.Ability> l = new ArrayList<>();
        l.add(ab("Удар", 1, "key.mouse.left", 0, ""));
        l.add(ab("Баррейдж", 2, "key.mouse.right", 0, ""));
        l.add(ab("Кислотная зона", 3, "key.mouse.right", 0, ""));
        l.add(ab("Кислотный плевок", 4, "key.mouse.right", 0, ""));
        l.add(ab("Диск: взрыв", 5, "key.mouse.right", 35, "Диски"));
        l.add(ab("Диск: остановка", 6, "key.mouse.right", 35, "Диски"));
        l.add(ab("Диск: прыжок", 7, "key.mouse.right", 35, "Диски"));
        l.add(ab("Кража стенда", 9, "key.mouse.right", 30, ""));
        return l;
    }

    public static List<Model.Macro> macros() {
        Model.Macro m = new Model.Macro();
        m.name = "Авто-кража (оглушить → украсть)";
        m.steps.add(Model.step(Model.StepType.AIM, s -> { s.target = Model.TargetKind.NEAREST_ANY; s.num = 10; s.ms = 150; }));
        m.steps.add(Model.step(Model.StepType.WAIT_UNTIL, s -> { s.cond = Model.CondType.AIMED_AT_TARGET; s.target = Model.TargetKind.NEAREST_ANY; s.num = 10; s.ms = 1500; s.onFail = Model.Fail.STOP; }));
        m.steps.add(Model.step(Model.StepType.USE_ABILITY, s -> { s.ability = "Диск: остановка"; s.onFail = Model.Fail.WAIT; }));
        m.steps.add(Model.step(Model.StepType.WAIT, s -> s.ms = 700));
        m.steps.add(Model.step(Model.StepType.USE_ABILITY, s -> { s.ability = "Кража стенда"; s.onFail = Model.Fail.WAIT; }));
        List<Model.Macro> l = new ArrayList<>();
        l.add(m);
        return l;
    }
}
