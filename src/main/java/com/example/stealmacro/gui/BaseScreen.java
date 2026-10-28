package com.example.stealmacro.gui;

import com.example.stealmacro.Keys;
import com.example.stealmacro.Model;
import com.example.stealmacro.Store;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public abstract class BaseScreen extends Screen {
    public static int selAbility = 0, selMacro = 0, selStep = -1;
    protected static final int LABEL_W = 120;

    protected int listW, fx, fy, fw, notes;
    protected final List<Object[]> labels = new ArrayList<>();
    private Button capBtn;
    private Consumer<String> capSink;
    private String capPrev = "";

    protected BaseScreen(String title) { super(Component.literal(title)); }

    protected abstract int tab();
    protected void renderBody(GuiGraphics g, int mx, int my) {}
    protected boolean clickBody(double mx, double my) { return false; }
    protected boolean scrollBody(double mx, double my, double d) { return false; }

    @Override protected void init() {
        labels.clear(); capBtn = null; notes = 0;
        listW = Math.max(120, width / 3 - 10);
        fx = listW + 24; fw = width - fx - 10; fy = 34;
        String[] names = {"Способности", "Макросы", "Шаги", "Настройки"};
        int tw = Math.min(110, (width - 20) / 4 - 2);
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            Button b = Button.builder(Component.literal(names[i]), x -> openTab(idx)).bounds(10 + i * (tw + 2), 5, tw, 20).build();
            b.active = idx != tab();
            addRenderableWidget(b);
        }
    }

    protected void openTab(int i) {
        Screen s = switch (i) {
            case 0 -> new AbilitiesScreen();
            case 1 -> new MacrosScreen();
            case 2 -> new StepsScreen();
            default -> new SettingsScreen();
        };
        minecraft.setScreen(s);
    }

    @Override public void removed() { Store.save(); }

    // ---------- виджеты ----------
    protected <T extends AbstractWidget> T row(String label, T w) {
        labels.add(new Object[]{label, fx, fy + 6});
        w.setPosition(fx + LABEL_W, fy);
        w.setWidth(Math.max(40, fw - LABEL_W));
        addRenderableWidget(w);
        fy += 22;
        return w;
    }

    protected Button btn(String text, int x, int y, int w, Runnable r) {
        Button b = Button.builder(Component.literal(text), bt -> r.run()).bounds(x, y, w, 20).build();
        addRenderableWidget(b);
        return b;
    }

    protected Button formBtn(String text, Runnable r) {
        Button b = btn(text, fx, fy, fw, r);
        fy += 22;
        return b;
    }

    protected void note(String t) { labels.add(new Object[]{t, fx, height - 14 - 10 * notes++}); }

    protected EditBox text(String label, String val, Consumer<String> on) {
        EditBox b = new EditBox(font, 0, 0, 100, 18, Component.literal(label));
        b.setMaxLength(200);
        b.setValue(val == null ? "" : val);
        b.setResponder(on);
        return row(label, b);
    }

    protected EditBox number(String label, double val, Consumer<Double> on) {
        EditBox b = new EditBox(font, 0, 0, 100, 18, Component.literal(label));
        b.setMaxLength(12);
        b.setFilter(s -> s.matches("-?\\d*\\.?\\d*"));
        b.setValue(fmt(val));
        b.setResponder(s -> { try { on.accept(Double.parseDouble(s)); } catch (NumberFormatException ignored) {} });
        return row(label, b);
    }

    protected static String fmt(double d) { return d == Math.rint(d) ? String.valueOf((long) d) : String.valueOf(d); }
    protected static double r2(double d) { return Math.round(d * 100.0) / 100.0; }

    protected Button choice(String label, List<String> opts, int cur, IntConsumer on) {
        int[] c = {cur};
        Button[] ref = new Button[1];
        java.util.function.Supplier<String> txt = () -> opts.isEmpty() ? "(пусто)"
                : (c[0] >= 0 && c[0] < opts.size() ? opts.get(c[0]) : "(выбрать)");
        ref[0] = Button.builder(Component.literal(txt.get()), b -> {
            if (opts.isEmpty()) return;
            c[0] = (c[0] + 1) % opts.size();
            on.accept(c[0]);
            ref[0].setMessage(Component.literal(txt.get()));
        }).bounds(0, 0, 100, 20).build();
        return row(label, ref[0]);
    }

    protected <E extends Enum<E> & Model.Labeled> Button cycle(String label, E cur, Consumer<E> on) {
        E[] vals = cur.getDeclaringClass().getEnumConstants();
        List<String> names = new ArrayList<>();
        for (E v : vals) names.add(v.label());
        return choice(label, names, cur.ordinal(), i -> on.accept(vals[i]));
    }

    protected Button keyButton(String label, String cur, Consumer<String> on) {
        Button[] ref = new Button[1];
        ref[0] = Button.builder(Component.literal(Keys.display(cur)), b -> {
            capBtn = ref[0]; capSink = on; capPrev = Keys.display(cur);
            ref[0].setMessage(Component.literal("> жми клавишу <"));
        }).bounds(0, 0, 100, 20).build();
        return row(label, ref[0]);
    }

    private void finishCapture(String name) {
        capSink.accept(name);
        capBtn.setMessage(Component.literal(Keys.display(name)));
        capBtn = null;
    }

    // ---------- listbox helper ----------
    protected ListView makeList(int sel, int bottomReserve) {
        ListView l = new ListView();
        l.x = 10; l.y = 34; l.w = listW; l.h = Math.max(40, height - 34 - bottomReserve); l.sel = sel;
        return l;
    }

    protected void listButtons(Runnable add, Runnable dup, Runnable del) {
        int y = height - 50, half = (listW - 4) / 2;
        btn("+ Новая", 10, y, half, add);
        btn("Копия", 10 + half + 4, y, half, dup);
        btn("Удалить", 10, y + 22, listW, del);
    }

    // ---------- события ----------
    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        super.render(g, mx, my, pt);
        for (Object[] l : labels) g.drawString(font, (String) l[0], (Integer) l[1], (Integer) l[2], 0xFFB0B0B0);
        renderBody(g, mx, my);
    }

    @Override public boolean keyPressed(int key, int scan, int mod) {
        if (capBtn != null) {
            if (key == GLFW.GLFW_KEY_ESCAPE) { capBtn.setMessage(Component.literal(capPrev)); capBtn = null; }
            else if (key == GLFW.GLFW_KEY_DELETE) finishCapture("");
            else finishCapture(Keys.nameOfKey(key, scan));
            return true;
        }
        boolean typing = getFocused() instanceof EditBox eb && eb.isFocused();
        if (!typing && Keys.MENU.matches(key, scan)) { onClose(); return true; }
        return super.keyPressed(key, scan, mod);
    }

    @Override public boolean mouseClicked(double mx, double my, int btn) {
        if (capBtn != null) { finishCapture(Keys.nameOfMouse(btn)); return true; }
        if (btn == 0 && clickBody(mx, my)) return true;
        return super.mouseClicked(mx, my, btn);
    }

    @Override public boolean mouseScrolled(double mx, double my, double sx, double sy) {
        if (scrollBody(mx, my, sy)) return true;
        return super.mouseScrolled(mx, my, sx, sy);
    }
          }
