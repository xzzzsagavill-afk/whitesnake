package com.example.stealmacro.gui;

import com.example.stealmacro.Defaults;
import com.example.stealmacro.Keys;
import com.example.stealmacro.Model;
import com.example.stealmacro.Store;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

public class AbilitiesScreen extends BaseScreen {
    private ListView list;

    public AbilitiesScreen() { super("Способности"); }
    @Override protected int tab() { return 0; }

    private List<String> rows() {
        List<String> r = new ArrayList<>();
        for (Model.Ability a : Store.data.abilities)
            r.add(a.name + "  [" + (a.slot > 0 ? "сл." + a.slot + " " : "") + Keys.display(a.key) + "]");
        return r;
    }

    @Override protected void init() {
        super.init();
        List<Model.Ability> as = Store.data.abilities;
        selAbility = as.isEmpty() ? -1 : Mth.clamp(selAbility, 0, as.size() - 1);
        list = makeList(selAbility, 56);
        list.rows = rows();

        listButtons(() -> {
            Model.Ability n = new Model.Ability();
            n.name = "Способность " + (as.size() + 1);
            as.add(n); selAbility = as.size() - 1; rebuildWidgets();
        }, () -> {
            if (selAbility < 0) return;
            Model.Ability c = Store.copy(as.get(selAbility), Model.Ability.class);
            c.name += " копия";
            as.add(c); selAbility = as.size() - 1; rebuildWidgets();
        }, () -> {
            if (selAbility < 0) return;
            as.remove(selAbility); selAbility = Math.max(0, selAbility - 1); rebuildWidgets();
        });

        if (selAbility >= 0) {
            Model.Ability a = as.get(selAbility);
            String[] cur = {a.name};
            text("Название", a.name, v -> { Store.renameAbility(cur[0], v); cur[0] = v; a.name = v; });
            choice("Слот хотбара", List.of("не менять", "1", "2", "3", "4", "5", "6", "7", "8", "9"), a.slot, i -> a.slot = i);
            keyButton("Кнопка / клик", a.key, k -> a.key = k);
            number("Удержание, мс", a.holdMs, v -> a.holdMs = v.intValue());
            number("Кулдаун, сек", a.cooldown, v -> a.cooldown = v);
            text("Группа КД", a.group, v -> a.group = v);
        }
        note("Одинаковая группа = общий кулдаун (диски 5-7).");
        note("Кнопка: любая клавиша (Shift, Space, F, R...) или клик. Esc - отмена, Del - очистить.");
        btn("+ Пресет White Snake", fx, height - 50 - 12 * 0, fw, () -> {
            for (Model.Ability d : Defaults.whiteSnake())
                if (as.stream().noneMatch(x -> x.name.equals(d.name))) as.add(d);
            rebuildWidgets();
        }).setY(height - 62);
    }

    @Override protected void renderBody(GuiGraphics g, int mx, int my) {
        list.rows = rows();
        list.render(g, font, mx, my);
    }
    @Override protected boolean clickBody(double mx, double my) {
        if (list.click(mx, my)) { selAbility = list.sel; rebuildWidgets(); return true; }
        return false;
    }
    @Override protected boolean scrollBody(double mx, double my, double d) { return list.wheel(mx, my, d); }
}
