package com.example.stealmacro;

import com.example.stealmacro.gui.AbilitiesScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.*;

public final class ClientEvents {
    private ClientEvents() {}

    public static void onTick(ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        while (Keys.MENU.consumeClick()) if (mc.screen == null) mc.setScreen(new AbilitiesScreen());
        while (Keys.STOP.consumeClick()) { if (Recorder.active) Recorder.finish(); else Runner.stop(); }
        Recorder.tick(mc);
        Runner.tick(mc);
    }

    public static void onKey(InputEvent.Key e) { handle(Keys.nameOfKey(e.getKey(), e.getScanCode()), e.getAction()); }
    public static void onMouse(InputEvent.MouseButton.Pre e) { handle(Keys.nameOfMouse(e.getButton()), e.getAction()); }

    public static void onChat(ClientChatReceivedEvent e) { Chat.add(e.getMessage().getString()); }

    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut e) { Runner.stop(); Recorder.cancel(); Cooldowns.clear(); }

    private static void handle(String name, int action) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player == null || action == 2) return;
        boolean press = action == 1;
        if (Recorder.active) { Recorder.onInput(name, press); return; }
        if (press) trackManual(mc, name);
        for (Model.Macro m : Store.data.macros) {
            if (m.key == null || m.key.isEmpty() || !m.key.equals(name)) continue;
            if (m.mode == Model.RunMode.HOLD) {
                if (press) Runner.start(m); else if (Runner.current() == m) Runner.stop();
            } else if (press) {
                if (Runner.current() == m) Runner.stop(); else Runner.start(m);
            }
        }
    }

    /** если ты сам нажал способность руками, мод тоже запускает её кулдаун */
    private static void trackManual(Minecraft mc, String name) {
        int sel = mc.player.getInventory().selected + 1;
        for (Model.Ability a : Store.data.abilities)
            if (a.cooldown > 0 && name.equals(a.key) && (a.slot == 0 || a.slot == sel)) Cooldowns.start(a);
    }
}
