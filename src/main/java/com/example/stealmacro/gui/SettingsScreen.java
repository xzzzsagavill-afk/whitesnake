package com.example.stealmacro.gui;

import com.example.stealmacro.Model;
import com.example.stealmacro.Store;

import java.util.List;

public class SettingsScreen extends BaseScreen {
    public SettingsScreen() { super("Настройки"); }
    @Override protected int tab() { return 3; }

    @Override protected void init() {
        super.init();
        // на этой вкладке нет списка: форма занимает всю ширину
        fx = 20; fw = width - 40;
        Model.Settings st = Store.data.settings;
        cycle("Камера при записи", st.camMode, v -> st.camMode = v);
        number("Разброс задержек, %", st.randomPercent, v -> st.randomPercent = v.intValue());
        number("Задержка слота, мс", st.slotDelayMs, v -> st.slotDelayMs = v.intValue());
        number("Допуск наводки, °", st.aimToleranceDeg, v -> st.aimToleranceDeg = v);
        number("Дальность записи", st.recordRange, v -> st.recordRange = v);
        choice("HUD", List.of("Выкл", "Вкл"), st.hud ? 1 : 0, i -> st.hud = i == 1);
        number("HUD X", st.hudX, v -> st.hudX = v.intValue());
        number("HUD Y", st.hudY, v -> st.hudY = v.intValue());
        note("«Цель, иначе поворот»: если при клике прицел на сущности - пишется шаг «навестись», иначе дословный поворот.");
        note("Тики по 50 мс: точнее задержки не бывает. Конфиг: config/stealmacro.json");
    }
}
