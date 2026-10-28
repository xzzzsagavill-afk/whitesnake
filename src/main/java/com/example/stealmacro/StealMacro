package com.example.stealmacro;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = StealMacro.MODID, dist = Dist.CLIENT)
public class StealMacro {
    public static final String MODID = "stealmacro";

    public StealMacro(IEventBus modBus, ModContainer container) {
        Store.load();
        modBus.addListener(Keys::register);
        modBus.addListener(StealMacro::registerHud);
        NeoForge.EVENT_BUS.addListener(ClientEvents::onTick);
        NeoForge.EVENT_BUS.addListener(ClientEvents::onKey);
        NeoForge.EVENT_BUS.addListener(ClientEvents::onMouse);
        NeoForge.EVENT_BUS.addListener(ClientEvents::onChat);
        NeoForge.EVENT_BUS.addListener(ClientEvents::onLogout);
    }

    private static void registerHud(RegisterGuiLayersEvent e) {
        e.registerAboveAll(ResourceLocation.fromNamespaceAndPath(MODID, "hud"), Hud::render);
    }
}
