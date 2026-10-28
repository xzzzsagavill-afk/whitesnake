package com.example.stealmacro;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Map;

public final class Hud {
    private Hud() {}

    public static void render(GuiGraphics g, DeltaTracker dt) {
        Model.Settings st = Store.data.settings;
        Minecraft mc = Minecraft.getInstance();
        if (!st.hud || mc.player == null || mc.options.hideGui) return;
        Font f = mc.font;
        int x = st.hudX, y = st.hudY;
        if (Recorder.active) {
            g.drawString(f, "● REC  шагов: " + Recorder.count() + "  [" + Keys.STOP.getTranslatedKeyMessage().getString() + "] = конец", x, y, 0xFFFF5555);
            y += 10;
        }
        Model.Macro m = Runner.current();
        if (m != null) {
            g.drawString(f, "▶ " + m.name + "  шаг " + Math.min(Runner.pc(), m.steps.size()) + "/" + m.steps.size(), x, y, 0xFF55FF55);
            y += 10;
        }
        for (Map.Entry<String, Long> e : Cooldowns.active().entrySet()) {
            g.drawString(f, e.getKey() + ": " + String.format("%.1f", e.getValue() / 1000.0) + "с", x, y, 0xFFFFFF55);
            y += 10;
        }
    }
}
