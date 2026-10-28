package com.example.stealmacro.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

/** Простой прокручиваемый список (не виджет, рисуется и обрабатывается из экрана). */
public class ListView {
    public int x, y, w, h, rowH = 14, scroll = 0, sel = -1;
    public List<String> rows = new ArrayList<>();

    public void render(GuiGraphics g, Font f, int mx, int my) {
        g.fill(x, y, x + w, y + h, 0xAA000000);
        g.enableScissor(x, y, x + w, y + h);
        for (int i = 0; i < rows.size(); i++) {
            int ry = y + i * rowH - scroll;
            if (ry + rowH < y || ry > y + h) continue;
            boolean hover = mx >= x && mx < x + w && my >= ry && my < ry + rowH && my >= y && my < y + h;
            if (i == sel) g.fill(x, ry, x + w, ry + rowH, 0xFF3A5FCD);
            else if (hover) g.fill(x, ry, x + w, ry + rowH, 0x33FFFFFF);
            g.drawString(f, f.plainSubstrByWidth(rows.get(i), w - 6), x + 3, ry + 3, 0xFFFFFFFF);
        }
        g.disableScissor();
    }

    public boolean click(double mx, double my) {
        if (mx < x || mx >= x + w || my < y || my >= y + h) return false;
        int i = (int) ((my - y + scroll) / rowH);
        if (i >= 0 && i < rows.size()) { sel = i; return true; }
        return false;
    }

    public boolean wheel(double mx, double my, double d) {
        if (mx < x || mx >= x + w || my < y || my >= y + h) return false;
        int max = Math.max(0, rows.size() * rowH - h);
        scroll = (int) Math.max(0, Math.min(max, scroll - d * rowH * 2));
        return true;
    }
}
