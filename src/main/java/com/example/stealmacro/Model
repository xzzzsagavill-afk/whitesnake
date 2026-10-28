package com.example.stealmacro;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Все данные, которые лежат в config/stealmacro.json */
public final class Model {
    private Model() {}

    public interface Labeled { String label(); }

    public enum StepType implements Labeled {
        USE_ABILITY("Использовать способность"),
        SELECT_SLOT("Выбрать слот"),
        PRESS_KEY("Нажать клавишу/клик"),
        KEY_DOWN("Зажать клавишу/клик"),
        KEY_UP("Отпустить клавишу/клик"),
        WAIT("Ждать (мс)"),
        ROTATE("Поворот камеры"),
        AIM("Навестись на цель"),
        LOOK_AT_POS("Смотреть на координаты"),
        WAIT_UNTIL("Ждать условие"),
        IF("Если условие → переход"),
        STOP("Остановить макрос");
        private final String l; StepType(String l) { this.l = l; } public String label() { return l; }
    }

    public enum TargetKind implements Labeled {
        NEAREST_PLAYER("Ближайший игрок"), NEAREST_MOB("Ближайший моб"), NEAREST_ANY("Ближайший любой");
        private final String l; TargetKind(String l) { this.l = l; } public String label() { return l; }
    }

    public enum CondType implements Labeled {
        AIMED_AT_TARGET("Прицел на цели"),
        TARGET_WITHIN("Цель ближе чем"),
        TARGET_BEYOND("Цель дальше/нет"),
        COOLDOWN_READY("КД готов"),
        COOLDOWN_ACTIVE("КД идёт"),
        HEALTH_BELOW("Моё HP не больше"),
        NEAR_POS("Я рядом с точкой"),
        CHAT_CONTAINS("Текст в чате/экшнбаре");
        private final String l; CondType(String l) { this.l = l; } public String label() { return l; }
    }

    public enum Fail { WAIT, SKIP, STOP }

    public enum RunMode implements Labeled {
        PRESS("Нажал = старт/стоп"), HOLD("Пока зажата");
        private final String l; RunMode(String l) { this.l = l; } public String label() { return l; }
    }

    public enum CamMode implements Labeled {
        TARGET("Цель, иначе поворот"), RAW("Только повороты");
        private final String l; CamMode(String l) { this.l = l; } public String label() { return l; }
    }

    public static class Ability {
        public String name = "Новая способность";
        /** 0 = не менять слот, 1..9 = слот хотбара */
        public int slot = 0;
        /** имя клавиши/кнопки мыши, напр. key.mouse.right, key.keyboard.r, key.keyboard.left.shift */
        public String key = "key.mouse.right";
        public int holdMs = 100;
        public double cooldown = 0;
        /** способности с одинаковой группой делят один кулдаун */
        public String group = "";
        public String groupKey() { return group == null || group.isBlank() ? name : group; }
    }

    public static class Step {
        public StepType type = StepType.WAIT;
        public int ms = 300;
        public int slot = 1;
        public String key = "";
        public String ability = "";
        public String text = "";
        public double x, y, z;
        public float yaw, pitch;
        public boolean relative;
        public double num = 6;
        public TargetKind target = TargetKind.NEAREST_PLAYER;
        public CondType cond = CondType.AIMED_AT_TARGET;
        /** 0 = следующий шаг, -1 = стоп, N = перейти на шаг N (с 1) */
        public int jumpTrue = 0, jumpFalse = 0;
        public Fail onFail = Fail.WAIT;
    }

    public static class Macro {
        public String name = "Новый макрос";
        public String key = "";
        public RunMode mode = RunMode.PRESS;
        public boolean loop = false;
        public List<Step> steps = new ArrayList<>();
    }

    public static class Settings {
        public CamMode camMode = CamMode.TARGET;
        public int randomPercent = 10;
        public int slotDelayMs = 50;
        public double aimToleranceDeg = 3;
        public double recordRange = 8;
        public boolean hud = true;
        public int hudX = 6, hudY = 6;
    }

    public static class Root {
        public boolean seeded = false;
        public Settings settings = new Settings();
        public List<Ability> abilities = new ArrayList<>();
        public List<Macro> macros = new ArrayList<>();
    }

    public static Step step(StepType t, Consumer<Step> c) {
        Step s = new Step();
        s.type = t;
        if (c != null) c.accept(s);
        return s;
    }
}
