package com.example.stealmacro;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public final class Keys {
    private Keys() {}

    public static final KeyMapping MENU = new KeyMapping("key.stealmacro.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "key.categories.stealmacro");
    public static final KeyMapping STOP = new KeyMapping("key.stealmacro.stop", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_END, "key.categories.stealmacro");

    public static void register(RegisterKeyMappingsEvent e) { e.register(MENU); e.register(STOP); }

    public static InputConstants.Key parse(String name) {
        if (name == null || name.isEmpty()) return null;
        try { return InputConstants.getKey(name); } catch (Exception e) { return null; }
    }

    public static String display(String name) {
        InputConstants.Key k = parse(name);
        return k == null ? "—" : k.getDisplayName().getString();
    }

    public static String nameOfKey(int key, int scan) { return InputConstants.getKey(key, scan).getName(); }
    public static String nameOfMouse(int button) { return InputConstants.Type.MOUSE.getOrCreate(button).getName(); }

    /** Эмуляция нажатия/отпускания через обычную систему KeyMapping (как будто нажал руками). */
    public static void set(String name, boolean down) {
        InputConstants.Key k = parse(name);
        if (k == null || k == InputConstants.UNKNOWN) return;
        KeyMapping.set(k, down);
        if (down) KeyMapping.click(k);
    }
}
