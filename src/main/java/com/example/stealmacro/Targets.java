package com.example.stealmacro;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class Targets {
    private Targets() {}

    public static List<Entity> candidates(Model.TargetKind k, double range) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        List<Entity> out = new ArrayList<>();
        if (p == null || mc.level == null) return out;
        for (Entity e : mc.level.getEntities(p, p.getBoundingBox().inflate(range))) {
            if (!(e instanceof LivingEntity le) || !le.isAlive() || e.isSpectator() || e instanceof ArmorStand) continue;
            boolean pl = e instanceof Player;
            if (k == Model.TargetKind.NEAREST_PLAYER && !pl) continue;
            if (k == Model.TargetKind.NEAREST_MOB && pl) continue;
            if (p.distanceTo(e) > range) continue;
            out.add(e);
        }
        return out;
    }

    public static Entity nearest(Model.TargetKind k, double range) {
        LocalPlayer p = Minecraft.getInstance().player;
        Entity best = null; double bd = Double.MAX_VALUE;
        for (Entity e : candidates(k, range)) {
            double d = p.distanceToSqr(e);
            if (d < bd) { bd = d; best = e; }
        }
        return best;
    }

    /** сущность, на которую прицел наведён (минимальный угол, не больше допуска) */
    public static Entity aimed(Model.TargetKind k, double range) {
        Entity best = null; double bd = Double.MAX_VALUE;
        for (Entity e : candidates(k, range)) {
            double a = angle(e);
            if (a <= tolerance(e) && a < bd) { bd = a; best = e; }
        }
        return best;
    }

    public static Vec3 center(Entity e) { return e.position().add(0, e.getBbHeight() * 0.5, 0); }

    public static double angle(Entity e) {
        LocalPlayer p = Minecraft.getInstance().player;
        Vec3 d = center(e).subtract(p.getEyePosition());
        double len = d.length();
        if (len < 1e-4) return 0;
        double dot = p.getLookAngle().dot(d.scale(1.0 / len));
        return Math.toDegrees(Math.acos(Mth.clamp(dot, -1.0, 1.0)));
    }

    public static double tolerance(Entity e) {
        LocalPlayer p = Minecraft.getInstance().player;
        double dist = Math.max(0.5, p.getEyePosition().distanceTo(center(e)));
        return Store.data.settings.aimToleranceDeg + Math.toDegrees(Math.atan2(e.getBbWidth() / 2.0, dist));
    }

    /** {yaw, pitch} чтобы смотреть на точку */
    public static float[] anglesTo(Vec3 v) {
        Vec3 eye = Minecraft.getInstance().player.getEyePosition();
        double dx = v.x - eye.x, dy = v.y - eye.y, dz = v.z - eye.z;
        double h = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, h));
        return new float[]{yaw, Mth.clamp(pitch, -90f, 90f)};
    }
                                                                    }
