package dev.theonlytazz.idlecinematics.client;

import dev.theonlytazz.idlecinematics.api.CinematicContext;
import dev.theonlytazz.idlecinematics.api.CinematicPreset;
import dev.theonlytazz.idlecinematics.api.CinematicRigState;
import dev.theonlytazz.idlecinematics.api.CinematicSubject;
import dev.theonlytazz.idlecinematics.api.TransitionSpec;
import dev.theonlytazz.idlecinematics.client.camera.CameraPose;
import dev.theonlytazz.idlecinematics.client.camera.CameraVolumeCollision;
import dev.theonlytazz.idlecinematics.client.camera.DampedRig;
import dev.theonlytazz.idlecinematics.client.camera.RigTransitions;
import dev.theonlytazz.idlecinematics.client.scene.SceneAnalyzer;
import dev.theonlytazz.idlecinematics.client.shots.ShotDirector;
import dev.theonlytazz.idlecinematics.client.shots.BoundedShotSelector;
import dev.theonlytazz.idlecinematics.client.shots.ShotPlan;
import dev.theonlytazz.idlecinematics.client.shots.ShotRegistry;
import dev.theonlytazz.idlecinematics.config.ClientConfig;
import dev.theonlytazz.idlecinematics.core.NamespacedId;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ClipContext;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.random.RandomGenerator;

public final class CinematicController {
    private static final int MAX_RESELECTION_ATTEMPTS = 3;
    private static final double TICK_SECONDS = 1.0 / 20.0;
    private static final double COLLISION_MARGIN = 0.35;

    private final RandomGenerator random;
    private final SceneAnalyzer sceneAnalyzer = new SceneAnalyzer();
    private final ShotDirector director;
    private final DampedRig damping = new DampedRig();
    private CameraPose previousPose;
    private CameraPose currentPose;
    private CinematicRigState semanticState;
    private ShotPlan plan;
    private CinematicContext context;
    private int shotTick;
    private double azimuthOffset;
    private boolean firstShotFrame;
    private CinematicRigState transitionOrigin;
    private CinematicSubject liveSubject;
    private boolean collided;
    private Object worldIdentity;

    public CinematicController() { this(RandomGenerator.getDefault()); }
    CinematicController(RandomGenerator random) {
        this.random = random;
        director = new ShotDirector(ShotRegistry.active(), random, CinematicController::presetEnabled);
    }

    public void start(Minecraft minecraft) {
        stop();
        worldIdentity = minecraft.level == null ? null : minecraft.level.dimension();
        chooseShot(minecraft);
        tick(minecraft);
        previousPose = currentPose;
    }

    /** Immediate restoration path: no interpolation or retained spring velocity survives. */
    public void stop() {
        previousPose = null;
        currentPose = null;
        semanticState = null;
        plan = null;
        liveSubject = null;
        transitionOrigin = null;
        context = null;
        shotTick = 0;
        collided = false;
        worldIdentity = null;
        damping.clear();
        director.reset();
        sceneAnalyzer.reset();
    }

    public void tick(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null || plan == null) return;
        if (!minecraft.level.dimension().equals(worldIdentity)) {
            worldIdentity = minecraft.level.dimension();
            chooseShot(minecraft);
        }
        if (plan.subject().type() == CinematicSubject.Type.ENTITY) {
            Optional<CinematicSubject> updated = sceneAnalyzer.updateEntitySubject(minecraft, plan.subject());
            if (updated.isEmpty()) { chooseShot(minecraft); return; }
            liveSubject = updated.orElseThrow();
        } else if (plan.subject().type() == CinematicSubject.Type.PLAYER) {
            liveSubject = new CinematicSubject(CinematicSubject.Type.PLAYER, Optional.of(minecraft.player.getUUID()),
                    minecraft.player.position().add(0.0, minecraft.player.getEyeHeight() * 0.62, 0.0),
                    minecraft.player.getBbHeight(), minecraft.player.getDeltaMovement().length());
        }
        previousPose = currentPose;
        if (shotTick >= plan.durationTicks()) chooseShot(minecraft);
        double progress = Math.min(1.0, shotTick / (double) Math.max(1, plan.durationTicks()));
        CinematicRigState desired = constrain(plan.motion().sample(progress, shotTick * TICK_SECONDS), plan.safety());
        if (liveSubject != null && (plan.subject().type() == CinematicSubject.Type.PLAYER || plan.subject().type() == CinematicSubject.Type.ENTITY)) {
            desired = translateForLiveSubject(desired, plan.subject(), liveSubject);
        }
        if (azimuthOffset != 0.0) desired = withAzimuth(desired, desired.azimuth() + azimuthOffset);
        TransitionSpec transition = effectiveTransition(plan.transition());
        if (firstShotFrame) {
            CinematicRigState prepared = RigTransitions.prepare(semanticState, desired, transition);
            transitionOrigin = prepared;
            if (transition.type() == TransitionSpec.Type.CONTINUE_ORBIT && semanticState != null) {
                azimuthOffset = prepared.azimuth() - desired.azimuth();
                desired = prepared;
            } else desired = prepared;
            firstShotFrame = false;
        }
        if (transitionOrigin != null && transition.type() == TransitionSpec.Type.MATCH_MOVE && transition.durationSeconds() > 0.0) {
            desired = RigTransitions.blend(transitionOrigin, desired, shotTick * TICK_SECONDS / transition.durationSeconds());
            if (shotTick * TICK_SECONDS >= transition.durationSeconds()) transitionOrigin = null;
        }
        semanticState = damping.update(desired, transition, TICK_SECONDS);
        Vec3 resolved = semanticState.resolvePosition(ClientConfig.CAMERA_DISTANCE.getAsDouble());
        CameraVolumeCollision.Result safe = collision(minecraft, semanticState.focus(), resolved, plan.safety().collisionRadius());
        collided = safe.collided();
        currentPose = lookAt(safe.position(), semanticState.focus(), semanticState.roll(), plan.safety().minimumPitch(), plan.safety().maximumPitch());
        shotTick++;
    }

    private void chooseShot(Minecraft minecraft) {
        context = sceneAnalyzer.analyze(minecraft, ClientConfig.INCLUDE_ENTITIES.getAsBoolean(), random);
        plan = BoundedShotSelector.choose(MAX_RESELECTION_ATTEMPTS,
                () -> director.next(context, ClientConfig.SHOT_MODE.get(), ClientConfig.SHOT_DURATION_SECONDS.getAsInt() * 20),
                candidate -> validPlan(minecraft, candidate), () -> guaranteedFallback(context));
        shotTick = 0;
        liveSubject = plan.subject();
        transitionOrigin = null;
        azimuthOffset = 0.0;
        firstShotFrame = true;
    }

    private ShotPlan guaranteedFallback(CinematicContext scene) {
        NamespacedId id = new NamespacedId("idlecinematics", scene.enclosed() ? "cave_close_portrait" : "tight_orbit");
        CinematicPreset fallback = ShotRegistry.active().require(id);
        CinematicSubject subject = scene.player();
        return new ShotPlan(fallback, subject, fallback.createMotion(scene, subject, random), 100,
                TransitionSpec.cut(), fallback.safety());
    }

    private boolean validPlan(Minecraft minecraft, ShotPlan candidate) {
        for (double sample : new double[]{0.0, 0.5, 1.0}) {
            CinematicRigState rig = constrain(candidate.motion().sample(sample, sample * candidate.durationTicks() * TICK_SECONDS), candidate.safety());
            Vec3 position = rig.resolvePosition(ClientConfig.CAMERA_DISTANCE.getAsDouble());
            CameraVolumeCollision.Result result = collision(minecraft, rig.focus(), position, candidate.safety().collisionRadius());
            if (result.distance() < candidate.safety().minimumDistance()) return false;
            if (!visible(minecraft, result.position(), rig.focus(), candidate.safety().obstructionTolerance())) return false;
            if (!fluidAllowed(minecraft, result.position(), candidate.safety().fluidPolicy())) return false;
        }
        return true;
    }

    private static boolean fluidAllowed(Minecraft minecraft, Vec3 position, dev.theonlytazz.idlecinematics.api.SafetyPolicy.FluidPolicy policy) {
        if (minecraft.level == null || policy == dev.theonlytazz.idlecinematics.api.SafetyPolicy.FluidPolicy.ALLOW_ALL) return true;
        var fluid = minecraft.level.getFluidState(net.minecraft.core.BlockPos.containing(position));
        if (fluid.isEmpty()) return true;
        return policy == dev.theonlytazz.idlecinematics.api.SafetyPolicy.FluidPolicy.ALLOW_WATER && fluid.is(FluidTags.WATER);
    }

    private CameraVolumeCollision.Result collision(Minecraft minecraft, Vec3 focus, Vec3 desired, double radius) {
        return CameraVolumeCollision.resolve(focus, desired, radius, COLLISION_MARGIN, (start, end) -> {
            if (minecraft.level == null || !minecraft.level.getChunkSource().hasChunk(
                    net.minecraft.util.Mth.floor(end.x) >> 4, net.minecraft.util.Mth.floor(end.z) >> 4)) return OptionalDouble.of(0.0);
            HitResult hit = minecraft.level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, minecraft.player));
            if (hit.getType() == HitResult.Type.MISS) return OptionalDouble.empty();
            double total = start.distanceTo(end);
            return OptionalDouble.of(total <= 1.0e-8 ? 0.0 : start.distanceTo(hit.getLocation()) / total);
        });
    }

    private static boolean visible(Minecraft minecraft, Vec3 camera, Vec3 focus, double tolerance) {
        if (minecraft.level == null) return false;
        HitResult hit = minecraft.level.clip(new ClipContext(camera, focus, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, minecraft.player));
        return hit.getType() == HitResult.Type.MISS || hit.getLocation().distanceTo(focus) <= tolerance;
    }

    private static CameraPose lookAt(Vec3 camera, Vec3 target, double roll, double minimumPitch, double maximumPitch) {
        float yaw = (float) (Math.toDegrees(Math.atan2(target.z - camera.z, target.x - camera.x)) - 90.0);
        double horizontal = Math.hypot(target.x - camera.x, target.z - camera.z);
        float pitch = (float) Math.max(minimumPitch, Math.min(maximumPitch,
                -Math.toDegrees(Math.atan2(target.y - camera.y, horizontal))));
        return new CameraPose(camera, yaw, pitch, (float) roll);
    }

    private TransitionSpec effectiveTransition(TransitionSpec requested) {
        if (!ClientConfig.SMOOTH_TRANSITIONS.getAsBoolean()) return TransitionSpec.cut();
        double intensity = ClientConfig.TRANSITION_INTENSITY.getAsDouble();
        TransitionSpec scaled = requested.scaled(intensity);
        if (scaled.type() == TransitionSpec.Type.DAMPED && intensity > 0.0) {
            return new TransitionSpec(scaled.type(), scaled.durationSeconds(), scaled.positionResponseSeconds() * intensity,
                    scaled.rotationResponseSeconds() * intensity, scaled.focusResponseSeconds() * intensity);
        }
        return scaled;
    }

    private static CinematicRigState withAzimuth(CinematicRigState state, double azimuth) {
        return new CinematicRigState(state.anchor(), state.focus(), state.distance(), azimuth, state.elevation(),
                state.lateralOffset(), state.verticalOffset(), state.roll(), state.cinematicFov(), state.subject(), state.yawMode());
    }

    private static CinematicRigState constrain(CinematicRigState state, dev.theonlytazz.idlecinematics.api.SafetyPolicy safety) {
        double distance = Math.max(safety.minimumDistance(), Math.min(safety.maximumDistance(), state.distance()));
        double elevation = Math.max(safety.minimumPitch(), Math.min(safety.maximumPitch(), state.elevation()));
        return new CinematicRigState(state.anchor(), state.focus(), distance, state.azimuth(), elevation,
                state.lateralOffset(), state.verticalOffset(), state.roll(), state.cinematicFov(), state.subject(), state.yawMode());
    }

    private static CinematicRigState translateForLiveSubject(CinematicRigState state, CinematicSubject original, CinematicSubject current) {
        Vec3 movement = current.focus().subtract(original.focus());
        return new CinematicRigState(state.anchor().add(movement), state.focus().add(movement), state.distance(), state.azimuth(),
                state.elevation(), state.lateralOffset(), state.verticalOffset(), state.roll(), state.cinematicFov(), current, state.yawMode());
    }

    private static boolean presetEnabled(CinematicPreset preset) {
        String pool = preset.pool();
        boolean poolEnabled = pool.equals("player") || pool.equals("cave") ? ClientConfig.PLAYER_POOL_ENABLED.getAsBoolean()
                : pool.equals("entity") ? ClientConfig.ENTITY_POOL_ENABLED.getAsBoolean()
                : pool.equals("sunrise") || pool.equals("day") || pool.equals("sunset") || pool.equals("night")
                        ? ClientConfig.CELESTIAL_POOL_ENABLED.getAsBoolean() : ClientConfig.LANDSCAPE_POOL_ENABLED.getAsBoolean();
        return poolEnabled && (ClientConfig.ENABLE_NEW_MOTIONS.getAsBoolean() || !isNewMotion(preset.id().path()));
    }

    private static boolean isNewMotion(String id) {
        return switch (id) {
            case "orbit", "tight_orbit", "wide_orbit", "hero_low", "profile", "over_shoulder", "overhead",
                    "push_in", "side_slide", "landscape_reveal", "landscape_crosspan", "entity_two_shot",
                    "cave_passage", "cave_wall_detail", "cave_close_portrait", "nether_ridge", "nether_passage",
                    "end_spire", "end_gateway_drift", "sunrise_horizon", "day_high_sky", "sunset_rim", "night_moonline" -> false;
            default -> true;
        };
    }

    public CameraPose sample(float partialTick) {
        if (currentPose == null || previousPose == null) return currentPose;
        double t = Math.max(0.0, Math.min(1.0, partialTick));
        return new CameraPose(previousPose.position().lerp(currentPose.position(), t),
                dev.theonlytazz.idlecinematics.core.SmoothMath.angle(previousPose.yaw(), currentPose.yaw(), t),
                (float) (previousPose.pitch() + (currentPose.pitch() - previousPose.pitch()) * t),
                (float) (previousPose.roll() + (currentPose.roll() - previousPose.roll()) * t));
    }

    public String selectedPresetDescription() { return plan == null ? "" : plan.pool() + " / " + plan.presetId(); }
    public String debugDescription() {
        if (plan == null || semanticState == null || context == null) return "";
        return plan.pool() + " / " + plan.presetId() + " | " + effectiveTransition(plan.transition()).type().name().toLowerCase()
                + " | " + plan.subject().type().name().toLowerCase() + " | "
                + String.format(java.util.Locale.ROOT, "%.1fm", semanticState.distance()) + (collided ? " | collision" : "")
                + " | " + (context.enclosed() ? "cave" : context.dimension().name().toLowerCase());
    }
    public boolean requiresPlayer() { return plan != null && (plan.subject().type() == CinematicSubject.Type.PLAYER || plan.preset().tags().contains("player")); }
    public Optional<UUID> entitySubjectId() { return plan == null || plan.subject().type() != CinematicSubject.Type.ENTITY ? Optional.empty() : plan.subject().entityId(); }
    public OptionalDouble requestedFov() {
        if (semanticState != null && semanticState.cinematicFov().isPresent()) return semanticState.cinematicFov();
        return ClientConfig.CINEMATIC_FOV_ENABLED.getAsBoolean() ? OptionalDouble.of(ClientConfig.CINEMATIC_FOV.getAsInt()) : OptionalDouble.empty();
    }
}
