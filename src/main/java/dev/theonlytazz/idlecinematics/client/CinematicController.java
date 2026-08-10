package dev.theonlytazz.idlecinematics.client;

import dev.theonlytazz.idlecinematics.config.ClientConfig;
import dev.theonlytazz.idlecinematics.core.SmoothMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ThreadLocalRandom;

public final class CinematicController {
    private static final int SHOT_TICKS = 20 * 14;
    private CameraPose previous;
    private CameraPose current;
    private Vec3 focus = Vec3.ZERO;
    private double phase;
    private int shotTicks;
    private boolean driftShot;
    private CameraType restorePerspective;
    private Boolean restoreSmartCull;

    public void start(Minecraft minecraft) {
        if (restorePerspective == null) restorePerspective = minecraft.options.getCameraType();
        if (restoreSmartCull == null) restoreSmartCull = minecraft.smartCull;
        minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        minecraft.smartCull = false;
        phase = ThreadLocalRandom.current().nextDouble(Math.PI * 2.0);
        shotTicks = SHOT_TICKS;
        driftShot = ClientConfig.PATH_MODE.get() == ClientConfig.PathMode.DRIFT;
        if (ClientConfig.PATH_MODE.get() == ClientConfig.PathMode.ALTERNATE) driftShot = ThreadLocalRandom.current().nextBoolean();
        current = null;
        tick(minecraft);
        previous = current;
    }

    public void stop() {
        previous = null;
        current = null;
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
        focus = minecraft.player.position().add(0.0, minecraft.player.getEyeHeight() * 0.62, 0.0);
        double speed = 0.012 * ClientConfig.PAN_SPEED.getAsDouble();
        phase += speed;
        if (--shotTicks <= 0) {
            shotTicks = SHOT_TICKS;
            driftShot = ClientConfig.PATH_MODE.get() == ClientConfig.PathMode.ALTERNATE ? !driftShot
                    : ClientConfig.PATH_MODE.get() == ClientConfig.PathMode.DRIFT;
        }
        double progress = 1.0 - shotTicks / (double) SHOT_TICKS;
        double radius = driftShot ? 7.5 + 2.0 * Math.sin(progress * Math.PI) : 5.5;
        double height = driftShot ? 2.5 + 1.5 * Math.sin(progress * Math.PI * 2.0) : 1.6 + 0.45 * Math.sin(phase * 0.7);
        double angle = driftShot ? phase * 0.55 : phase;
        Vec3 desired = focus.add(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
        Vec3 safe = avoidClipping(minecraft, focus, desired);
        float yaw = (float) (Math.toDegrees(Math.atan2(focus.z - safe.z, focus.x - safe.x)) - 90.0);
        double horizontal = Math.hypot(focus.x - safe.x, focus.z - safe.z);
        float pitch = (float) -Math.toDegrees(Math.atan2(focus.y - safe.y, horizontal));
        current = new CameraPose(safe, yaw, Math.max(-82.0f, Math.min(82.0f, pitch)), 0.0f);
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
        double t = ClientConfig.SMOOTH_TRANSITIONS.get() ? SmoothMath.smootherStep(partialTick) : partialTick;
        Vec3 pos = previous.position().lerp(current.position(), t);
        return new CameraPose(pos, SmoothMath.angle(previous.yaw(), current.yaw(), t),
                (float) (previous.pitch() + (current.pitch() - previous.pitch()) * t), 0.0f);
    }
}
