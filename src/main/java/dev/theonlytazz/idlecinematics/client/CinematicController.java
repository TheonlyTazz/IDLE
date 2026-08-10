package dev.theonlytazz.idlecinematics.client;

import dev.theonlytazz.idlecinematics.config.ClientConfig;
import dev.theonlytazz.idlecinematics.core.SmoothMath;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class CinematicController {
    private CameraPose previous;
    private CameraPose current;
    private CameraPose transitionFrom;
    private Vec3 focus = Vec3.ZERO;
    private ShotPreset preset = ShotPreset.ORBIT;
    private double phase;
    private int shotTick;
    private int shotLength;
    private CameraType restorePerspective;
    private Boolean restoreSmartCull;

    public void start(Minecraft minecraft) {
        if (restorePerspective == null) restorePerspective = minecraft.options.getCameraType();
        if (restoreSmartCull == null) restoreSmartCull = minecraft.smartCull;
        minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        minecraft.smartCull = false;
        phase = ThreadLocalRandom.current().nextDouble(Math.PI * 2.0);
        current = null;
        chooseShot(minecraft);
        tick(minecraft);
        previous = current;
    }

    public void stop() {
        previous = null;
        current = null;
        transitionFrom = null;
        if (restorePerspective != null) {
            Minecraft.getInstance().options.setCameraType(restorePerspective);
            restorePerspective = null;
        }
        if (restoreSmartCull != null) {
            Minecraft.getInstance().smartCull = restoreSmartCull;
            restoreSmartCull = null;
        }
    }

    public void tick(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null) return;
        minecraft.smartCull = false;
        previous = current;
        if (shotTick++ >= shotLength) chooseShot(minecraft);

        Vec3 playerFocus = minecraft.player.position().add(0.0, minecraft.player.getEyeHeight() * 0.62, 0.0);
        focus = preset == ShotPreset.COMPANION ? companionFocus(minecraft, playerFocus) : playerFocus;
        double speed = 0.011 * ClientConfig.PAN_SPEED.getAsDouble();
        phase += speed;
        double t = shotTick / (double) shotLength;
        double distance = ClientConfig.CAMERA_DISTANCE.getAsDouble();
        Vec3 offset = offsetFor(preset, phase, t).scale(distance);
        Vec3 desired = focus.add(offset);
        Vec3 safe = avoidClipping(minecraft, focus, desired);
        current = lookAt(safe, focus);

        if (transitionFrom != null) {
            double blend = SmoothMath.smootherStep(Math.min(1.0, shotTick / 36.0));
            current = interpolate(transitionFrom, current, blend);
            if (shotTick >= 36) transitionFrom = null;
        }
    }

    private void chooseShot(Minecraft minecraft) {
        transitionFrom = current;
        shotTick = 0;
        shotLength = ClientConfig.SHOT_DURATION_SECONDS.getAsInt() * 20;
        boolean openSky = minecraft.player != null && minecraft.level != null
                && minecraft.level.canSeeSky(BlockPos.containing(minecraft.player.getEyePosition()));
        boolean underground = minecraft.player != null && minecraft.player.getY() < minecraft.level.getSeaLevel() - 8 && !openSky;
        boolean openArea = isOpenArea(minecraft);
        long dayTime = minecraft.level.getDayTime() % 24000L;
        boolean goldenHour = dayTime < 2000L || (dayTime > 11000L && dayTime < 14000L);
        boolean companion = ClientConfig.INCLUDE_ENTITIES.getAsBoolean() && hasCompanion(minecraft);
        ClientConfig.ShotMode mode = ClientConfig.SHOT_MODE.get();
        List<ShotPreset> candidates = new ArrayList<>();
        for (ShotPreset shot : ShotPreset.values()) {
            if (shot == preset || (shot.needsSky && !openSky) || (shot.underground && !underground)
                    || (shot == ShotPreset.COMPANION && !companion)) continue;
            if (!openArea && (shot == ShotPreset.WIDE_ORBIT || shot == ShotPreset.REVEAL || shot == ShotPreset.SKYLINE)) continue;
            if (mode == ClientConfig.ShotMode.PLAYER_FOCUSED && shot.environment) continue;
            if (mode == ClientConfig.ShotMode.ENVIRONMENT_FOCUSED && !shot.environment) continue;
            if (mode == ClientConfig.ShotMode.CLASSIC && shot != ShotPreset.ORBIT && shot != ShotPreset.WIDE_ORBIT) continue;
            candidates.add(shot);
            if ((underground && shot == ShotPreset.CAVE_TRACK) || (openSky && shot.environment) || (companion && shot == ShotPreset.COMPANION)) {
                candidates.add(shot); // context-fit shots receive an extra draw weight
            }
            if (goldenHour && shot == ShotPreset.GOLDEN_HOUR) {
                candidates.add(shot);
                candidates.add(shot);
            }
        }
        if (candidates.isEmpty()) candidates.add(ShotPreset.ORBIT);
        preset = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        phase += ThreadLocalRandom.current().nextDouble(0.35, 1.1);
    }

    private Vec3 offsetFor(ShotPreset shot, double angle, double t) {
        double side = Math.sin(angle);
        double forward = Math.cos(angle);
        return switch (shot) {
            case ORBIT -> new Vec3(forward * 5.4, 1.7 + Math.sin(angle * 0.6) * 0.35, side * 5.4);
            case WIDE_ORBIT -> new Vec3(forward * 9.0, 3.8 + Math.sin(t * Math.PI) * 1.2, side * 9.0);
            case HERO_LOW -> new Vec3(forward * 4.2, -0.45 + Math.sin(t * Math.PI) * 0.45, side * 4.2);
            case PROFILE -> new Vec3(forward * 3.8, 1.15, side * 3.8);
            case OVER_SHOULDER -> new Vec3(forward * 2.25, 1.55, side * 2.25);
            case OVERHEAD -> new Vec3(forward * 2.6, 7.5, side * 2.6);
            case SKYLINE -> new Vec3(forward * 11.5, 5.5 + t * 1.5, side * 11.5);
            case GOLDEN_HOUR -> new Vec3(forward * 8.5, 2.8 + Math.sin(t * Math.PI) * 0.8, side * 8.5);
            case REVEAL -> new Vec3(forward * (3.5 + t * 8.0), 1.0 + t * 5.0, side * (3.5 + t * 8.0));
            case CAVE_TRACK -> new Vec3(forward * 3.2, 0.75 + Math.sin(t * Math.PI) * 0.5, side * 3.2);
            case COMPANION -> new Vec3(forward * 6.0, 2.25, side * 6.0);
        };
    }

    private boolean hasCompanion(Minecraft minecraft) {
        return minecraft.player != null && minecraft.level != null && minecraft.level.getEntitiesOfClass(LivingEntity.class,
                minecraft.player.getBoundingBox().inflate(10.0), entity -> entity != minecraft.player && entity.isAlive()).size() > 0;
    }

    private boolean isOpenArea(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null) return false;
        Vec3 origin = minecraft.player.getEyePosition();
        int clear = 0;
        for (Vec3 direction : List.of(new Vec3(1, 0, 0), new Vec3(-1, 0, 0), new Vec3(0, 0, 1), new Vec3(0, 0, -1))) {
            HitResult hit = minecraft.level.clip(new ClipContext(origin, origin.add(direction.scale(8.0)),
                    ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, minecraft.player));
            if (hit.getType() == HitResult.Type.MISS) clear++;
        }
        return clear >= 3;
    }

    private Vec3 companionFocus(Minecraft minecraft, Vec3 fallback) {
        if (minecraft.player == null || minecraft.level == null) return fallback;
        return minecraft.level.getEntitiesOfClass(LivingEntity.class, new AABB(fallback, fallback).inflate(10.0),
                        entity -> entity != minecraft.player && entity.isAlive()).stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(minecraft.player)))
                .map(entity -> fallback.lerp(entity.getBoundingBox().getCenter(), 0.42)).orElse(fallback);
    }

    private CameraPose lookAt(Vec3 camera, Vec3 target) {
        float yaw = (float) (Math.toDegrees(Math.atan2(target.z - camera.z, target.x - camera.x)) - 90.0);
        double horizontal = Math.hypot(target.x - camera.x, target.z - camera.z);
        float pitch = (float) -Math.toDegrees(Math.atan2(target.y - camera.y, horizontal));
        return new CameraPose(camera, yaw, Math.max(-82.0f, Math.min(82.0f, pitch)), 0.0f);
    }

    private CameraPose interpolate(CameraPose from, CameraPose to, double t) {
        return new CameraPose(from.position().lerp(to.position(), t), SmoothMath.angle(from.yaw(), to.yaw(), t),
                (float) (from.pitch() + (to.pitch() - from.pitch()) * t), 0.0f);
    }

    private Vec3 avoidClipping(Minecraft minecraft, Vec3 target, Vec3 desired) {
        HitResult hit = minecraft.level.clip(new ClipContext(target, desired, ClipContext.Block.VISUAL,
                ClipContext.Fluid.NONE, minecraft.player));
        if (hit.getType() == HitResult.Type.MISS) return desired;
        Vec3 direction = desired.subtract(target).normalize();
        return hit.getLocation().subtract(direction.scale(0.35));
    }

    public CameraPose sample(float partialTick) {
        if (current == null || previous == null) return current;
        double t = ClientConfig.SMOOTH_TRANSITIONS.getAsBoolean() ? SmoothMath.smootherStep(partialTick) : partialTick;
        return interpolate(previous, current, t);
    }
}
