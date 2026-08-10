package dev.theonlytazz.idlecinematics.client;

import dev.theonlytazz.idlecinematics.config.ClientConfig;
import dev.theonlytazz.idlecinematics.core.SmoothMath;
import dev.theonlytazz.idlecinematics.client.camera.*;
import dev.theonlytazz.idlecinematics.client.scene.*;
import dev.theonlytazz.idlecinematics.client.shots.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CinematicController {
    private static final Logger LOGGER = LoggerFactory.getLogger("Idle Cinematics");
    private static final double MIN_USEFUL_CAMERA_DISTANCE = 1.15;
    private static final int MAX_BLOCKED_TICKS = 6;

    private final SceneAnalyzer sceneAnalyzer = new SceneAnalyzer();
    private final ShotDirector director = new ShotDirector(ShotRegistry.builtIns());
    private CameraPose previous;
    private CameraPose current;
    private CameraPose transitionFrom;
    private ShotPlan plan;
    private double phase;
    private int shotTick;
    private int blockedTicks;
    private CameraType restorePerspective;
    private Boolean restoreSmartCull;

    public void start(Minecraft minecraft) {
        if (restorePerspective == null) restorePerspective = minecraft.options.getCameraType();
        if (restoreSmartCull == null) restoreSmartCull = minecraft.smartCull;
        minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        minecraft.smartCull = false;
        minecraft.levelRenderer.needsUpdate();
        phase = ThreadLocalRandom.current().nextDouble(Math.PI * 2.0);
        current = null;
        director.reset();
        chooseShot(minecraft);
        tick(minecraft);
        previous = current;
    }

    public void stop() {
        previous = null;
        current = null;
        transitionFrom = null;
        plan = null;
        director.reset();
        if (restorePerspective != null) {
            Minecraft.getInstance().options.setCameraType(restorePerspective);
            restorePerspective = null;
        }
        if (restoreSmartCull != null) {
            Minecraft.getInstance().smartCull = restoreSmartCull;
            restoreSmartCull = null;
        }
        Minecraft.getInstance().levelRenderer.needsUpdate();
    }

    public void tick(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null || plan == null) return;
        minecraft.smartCull = false;
        previous = current;
        if (shotTick++ >= plan.durationTicks()) chooseShot(minecraft);

        phase += 0.011 * ClientConfig.PAN_SPEED.getAsDouble();
        double progress = Math.min(1.0, shotTick / (double) plan.durationTicks());
        Vec3 desired = plan.desiredPosition(progress, phase, ClientConfig.CAMERA_DISTANCE.getAsDouble());
        Vec3 safe = avoidClipping(minecraft, plan.cameraAnchor(), desired);
        if (safe.distanceTo(plan.cameraAnchor()) < MIN_USEFUL_CAMERA_DISTANCE) blockedTicks++; else blockedTicks = 0;
        if (blockedTicks > MAX_BLOCKED_TICKS) {
            chooseShot(minecraft);
            return;
        }
        current = lookAt(safe, plan.focus());

        if (transitionFrom != null) {
            double blend = SmoothMath.smootherStep(Math.min(1.0, shotTick / (double) plan.transitionTicks()));
            current = interpolate(transitionFrom, current, blend);
            if (shotTick >= plan.transitionTicks()) transitionFrom = null;
        }
    }

    private void chooseShot(Minecraft minecraft) {
        transitionFrom = current;
        shotTick = 0;
        blockedTicks = 0;
        SceneContext scene = sceneAnalyzer.analyze(minecraft, ClientConfig.INCLUDE_ENTITIES.getAsBoolean());
        plan = director.next(scene, ClientConfig.SHOT_MODE.get(), ClientConfig.SHOT_DURATION_SECONDS.getAsInt() * 20);
        LOGGER.info("Selected cinematic preset: {} / {} [dimension={}, phase={}, enclosed={}, openDirections={}, entityTarget={}]",
                plan.pool().name().toLowerCase(), plan.presetId(), scene.dimension(), scene.dayPhase(), scene.enclosed(),
                scene.openDirections(), scene.nearbyEntityFocus().isPresent());
        phase += ThreadLocalRandom.current().nextDouble(0.35, 1.1);
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

    public String selectedPresetDescription() {
        return plan == null ? "" : plan.pool().name().toLowerCase() + " / " + plan.presetId();
    }
}
