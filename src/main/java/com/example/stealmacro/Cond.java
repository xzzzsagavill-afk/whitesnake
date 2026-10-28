package com.example.stealmacro;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class Cond {
    private Cond() {}

    /** since = время (мс), после которого учитываем сообщения чата */
    public static boolean eval(Model.Step s, long since) {
        LocalPlayer p = Minecraft.getInstance().player;
        if (p == null) return false;
        switch (s.cond) {
            case AIMED_AT_TARGET:
                return Targets.aimed(s.target, s.num) != null;
            case TARGET_WITHIN: {
                Entity t = Targets.nearest(s.target, 256);
                return t != null && p.distanceTo(t) <= s.num;
            }
            case TARGET_BEYOND: {
                Entity t = Targets.nearest(s.target, 256);
                return t == null || p.distanceTo(t) > s.num;
            }
            case COOLDOWN_READY: {
                Model.Ability a = Cooldowns.find(s.ability);
                return a != null && Cooldowns.ready(a);
            }
            case COOLDOWN_ACTIVE: {
                Model.Ability a = Cooldowns.find(s.ability);
                return a != null && !Cooldowns.ready(a);
            }
            case HEALTH_BELOW:
                return p.getHealth() <= s.num;
            case NEAR_POS:
                return p.position().distanceTo(new Vec3(s.x, s.y, s.z)) <= s.num;
            case CHAT_CONTAINS:
                return Chat.seen(s.text, since);
        }
        return false;
    }
          }
