package com.example.stealmacro.gui;

import com.example.stealmacro.Keys;
import com.example.stealmacro.Model;
import com.example.stealmacro.Recorder;
import com.example.stealmacro.Runner;
import com.example.stealmacro.Store;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class MacrosScreen extends BaseScreen {
    private ListView list;

    public MacrosScreen() { super("Макросы"); }
    @Override protected int tab() { return 1; }

    private List<String> rows() {
        List<String> r = new ArrayList<>();
        for (Model.Macro m : Store.data.macros)
            r.add(m.name + (m.key == null || m.key.isEmpty() ? "" : "  [" + Keys.display(m.key) + "]"));
        return r;
    }

    @Override protected void init() {
        super.init();
        List<Model.Macro> ms = Store.data.macros;
        selMacro = ms.isEmpty() ? -1 : Mth.clamp(selMacro, 0, ms.size() - 1);
        list = makeList(selMacro, 56);
        list.rows = rows();

        listButtons(() -> {
            Model.Macro n = new Model.Macro();
            n.name = "Макрос " + (ms.size() + 1);
            ms.add(n); selMacro = ms.size() - 1; rebuildWidgets();
        }, () -> {
            if (selMacro < 0) return;
            Model.Macro c = Store.copy(ms.get(selMacro), Model.Macro.class);
            c.name += " копия"; c.key = "";
            ms.add(c); selMacro = ms.size() - 1; rebuildWidgets();
        }, () -> {
            if (selMacro < 0) return;
            ms.remove(selMacro); selMacro = Math.max(0, selMacro - 1); rebuildWidgets();
        });

        if (selMacro >= 0) {
            Model.Macro m = ms.get(selMacro);
            text("Название", m.name, v -> m.name = v);
            keyButton("Клавиша запуска", m.key, k -> m.key = k);
            cycle("Режим", m.mode, v -> m.mode = v);
            choice("Зацикливать", List.of("Нет", "Да"), m.loop ? 1 : 0, i -> m.loop = i == 1);
            labels.add(new Object[]{"Шагов в макросе: " + m.steps.size(), fx, fy + 4});
            fy += 18;
            formBtn("▶ Запустить сейчас", () -> { onClose(); Runner.start(m); });
            formBtn("● Запись (добавить в конец)", () -> { onClose(); Recorder.start(m); });
            formBtn("Редактировать шаги →", () -> { selStep = m.steps.isEmpty() ? -1 : 0; openTab(2); });
        }
        note("Запись: закрывается меню, делай всё руками, потом клавиша «Стоп» (по умолч. End).");
        note("Клавиша запуска: лучше свободная (G, V, боковые кнопки мыши), иначе сработает и её обычное действие.");
    }

    @Override protected void renderBody(GuiGraphics g, int mx, int my) {
        list.rows = rows();
        list.render(g, font, mx, my);
    }
    @Override protected boolean clickBody(double mx, double my) {
        if (list.click(mx, my)) { selMacro = list.sel; selStep = 0; rebuildWidgets(); return true; }
        return false;
    }
    @Override protected boolean scrollBody(double mx, double my, double d) { return list.wheel(mx, my, d); }
}
