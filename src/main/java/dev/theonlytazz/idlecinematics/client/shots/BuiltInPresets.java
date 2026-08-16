package dev.theonlytazz.idlecinematics.client.shots;

import dev.theonlytazz.idlecinematics.IdleCinematics;
import dev.theonlytazz.idlecinematics.api.CameraMotion;
import dev.theonlytazz.idlecinematics.api.CinematicContext;
import dev.theonlytazz.idlecinematics.api.CinematicPreset;
import dev.theonlytazz.idlecinematics.api.CinematicRigState;
import dev.theonlytazz.idlecinematics.api.CinematicSubject;
import dev.theonlytazz.idlecinematics.api.SafetyPolicy;
import dev.theonlytazz.idlecinematics.api.TransitionSpec;
import dev.theonlytazz.idlecinematics.core.NamespacedId;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import java.util.random.RandomGenerator;

/** Original, semantic motion library. Built-ins deliberately consume the same public API as add-ons. */
public final class BuiltInPresets {
    private BuiltInPresets() {}

    public static List<CinematicPreset> create() {
        List<CinematicPreset> presets = new ArrayList<>();
        presets.add(preset("orbit", "player", tags("player"), any(), Motion.ORBIT, TransitionSpec.continueOrbit(), SafetyPolicy.standard()));
        presets.add(preset("tight_orbit", "player", tags("player", "close"), any(), Motion.TIGHT_ORBIT, TransitionSpec.damped(0.55), SafetyPolicy.standard()));
        presets.add(preset("wide_orbit", "player", tags("player", "wide", "open_sky"), wide(), Motion.CROWN_ORBIT, TransitionSpec.continueOrbit(), SafetyPolicy.standard()));
        presets.add(preset("hero_low", "player", tags("player", "low"), low(), Motion.HERO_SWEEP, TransitionSpec.matchMove(), SafetyPolicy.standard()));
        presets.add(preset("profile", "player", tags("player", "close"), any(), Motion.HELICAL_PORTRAIT, TransitionSpec.damped(0.65), SafetyPolicy.standard()));
        presets.add(preset("over_shoulder", "player", tags("player", "close"), any(), Motion.ISOMETRIC_HOLD, TransitionSpec.damped(0.5), SafetyPolicy.standard()));
        presets.add(preset("overhead", "player", tags("player", "wide"), context -> context.ceilingClearance() >= 8.0 ? 1.2 : 0.0, Motion.CRANE, TransitionSpec.matchMove(), SafetyPolicy.standard()));
        presets.add(preset("push_in", "player", tags("player"), any(), Motion.EXPEDITION, TransitionSpec.matchMove(), SafetyPolicy.standard()));
        presets.add(preset("side_slide", "player", tags("player"), any(), Motion.FIGURE_EIGHT, TransitionSpec.continueOrbit(), SafetyPolicy.standard()));
        presets.add(preset("breathing_orbit", "player", tags("player"), any(), Motion.BREATHING_ORBIT, TransitionSpec.continueOrbit(), SafetyPolicy.standard()));

        presets.add(preset("terrain_scout", "landscape", tags("environment", "wide"), terrain(), Motion.TERRAIN_SCOUT, TransitionSpec.matchMove(), SafetyPolicy.standard()));
        presets.add(preset("foreground_parallax", "landscape", tags("environment", "parallax"), parallax(), Motion.PARALLAX, TransitionSpec.matchMove(), SafetyPolicy.standard()));
        presets.add(preset("landscape_reveal", "landscape", tags("environment", "wide", "open_sky"), wide(), Motion.CRANE, TransitionSpec.damped(0.8), SafetyPolicy.standard()));
        presets.add(preset("landscape_crosspan", "landscape", tags("environment", "wide"), terrain(), Motion.ISOMETRIC_HOLD, TransitionSpec.matchMove(), SafetyPolicy.standard()));
        presets.add(preset("rain_silhouette", "landscape", tags("environment", "weather"),
                context -> context.weather() == CinematicContext.Weather.CLEAR ? 0.0 : 1.7,
                Motion.PARALLAX, TransitionSpec.damped(0.6), SafetyPolicy.standard()));

        presets.add(preset("entity_two_shot", "entity", tags("environment", "entity"), entity(), Motion.TWO_SHOT, TransitionSpec.matchMove(), SafetyPolicy.standard()));
        presets.add(preset("entity_portrait", "entity", tags("environment", "entity", "close"), entity(), Motion.HELICAL_PORTRAIT, TransitionSpec.damped(0.55), SafetyPolicy.standard()));

        presets.add(preset("cave_passage", "cave", tags("player", "cave", "close"), cave(), Motion.EXPEDITION, TransitionSpec.damped(0.45), SafetyPolicy.cave()));
        presets.add(preset("cave_wall_detail", "cave", tags("environment", "cave", "close"), cave(), Motion.ISOMETRIC_HOLD, TransitionSpec.damped(0.4), SafetyPolicy.cave()));
        presets.add(preset("cave_close_portrait", "cave", tags("player", "cave", "close"), cave(), Motion.TIGHT_ORBIT, TransitionSpec.damped(0.35), SafetyPolicy.cave()));

        presets.add(preset("nether_ridge", "nether", tags("environment", "nether"), dimension(CinematicContext.DimensionKind.NETHER), Motion.TERRAIN_SCOUT, TransitionSpec.matchMove(), dimensionSafety(10.0)));
        presets.add(preset("nether_passage", "nether", tags("player", "nether"), dimension(CinematicContext.DimensionKind.NETHER), Motion.EXPEDITION, TransitionSpec.damped(0.55), dimensionSafety(8.0)));
        presets.add(preset("end_spire", "end", tags("environment", "end", "wide"), dimension(CinematicContext.DimensionKind.END), Motion.CROWN_ORBIT, TransitionSpec.matchMove(), dimensionSafety(12.0)));
        presets.add(preset("end_gateway_drift", "end", tags("player", "end"), dimension(CinematicContext.DimensionKind.END), Motion.BREATHING_ORBIT, TransitionSpec.continueOrbit(), dimensionSafety(10.0)));

        presets.add(sky("sunrise_horizon", "sunrise", CinematicContext.DayPhase.SUNRISE));
        presets.add(sky("day_high_sky", "day", CinematicContext.DayPhase.DAY));
        presets.add(sky("sunset_rim", "sunset", CinematicContext.DayPhase.SUNSET));
        presets.add(sky("night_moonline", "night", CinematicContext.DayPhase.NIGHT));
        return List.copyOf(presets);
    }

    private static CinematicPreset sky(String id, String pool, CinematicContext.DayPhase phase) {
        return preset(id, pool, tags("environment", "open_sky", "celestial"),
                context -> context.openSky() && context.dayPhase() == phase ? 2.4 : 0.0,
                Motion.CELESTIAL, TransitionSpec.matchMove(), dimensionSafety(10.0));
    }

    private static CinematicPreset preset(String id, String pool, Set<String> tags, ToDoubleFunction<CinematicContext> score,
                                          Motion motion, TransitionSpec transition, SafetyPolicy safety) {
        return new BuiltIn(new NamespacedId(IdleCinematics.MOD_ID, id), pool, tags, score, motion, transition, safety);
    }

    private static ToDoubleFunction<CinematicContext> any() { return context -> 1.0; }
    private static ToDoubleFunction<CinematicContext> cave() { return context -> context.enclosed() ? 2.2 : 0.0; }
    private static ToDoubleFunction<CinematicContext> entity() { return context -> context.selectedSubject().filter(subject -> subject.type() == CinematicSubject.Type.ENTITY).isPresent() ? 2.1 : 0.0; }
    private static ToDoubleFunction<CinematicContext> terrain() { return context -> context.terrainTarget().isPresent() ? 1.5 : 0.0; }
    private static ToDoubleFunction<CinematicContext> parallax() { return context -> context.parallaxDirection().isPresent() ? 2.0 : 0.0; }
    private static ToDoubleFunction<CinematicContext> wide() { return context -> context.openSky() && context.openArea() && context.effectiveRenderDistance() >= 8 ? 1.8 : 0.0; }
    private static ToDoubleFunction<CinematicContext> low() { return context -> context.floorDrop() < 2.5 && context.mostOpenDirection().filter(probe -> probe.cameraClearance() >= 4.0).isPresent() ? 1.3 : 0.0; }
    private static ToDoubleFunction<CinematicContext> dimension(CinematicContext.DimensionKind kind) { return context -> context.dimension() == kind ? 2.3 : 0.0; }
    private static Set<String> tags(String... tags) { return Set.of(tags); }
    private static SafetyPolicy dimensionSafety(double maximum) { return new SafetyPolicy(1.1, maximum, -76, 76, 0.22, SafetyPolicy.FluidPolicy.REJECT, 0.12); }

    private record BuiltIn(NamespacedId id, String pool, Set<String> tags, ToDoubleFunction<CinematicContext> scorer,
                           Motion motion, TransitionSpec transition, SafetyPolicy safety) implements CinematicPreset {
        @Override public double contextScore(CinematicContext context) { return Math.max(0.0, scorer.applyAsDouble(context)); }
        @Override public CinematicSubject selectSubject(CinematicContext context, RandomGenerator random) {
            if (tags.contains("entity")) return context.selectedSubject().orElse(context.player());
            if (tags.contains("celestial")) return context.celestialTarget().orElse(context.player());
            if (tags.contains("environment")) return context.terrainTarget().orElse(context.player());
            return context.player();
        }
        @Override public CameraMotion createMotion(CinematicContext context, CinematicSubject subject, RandomGenerator random) {
            return motion.create(context, subject, random);
        }
        @Override public DurationRange duration() { return tags.contains("cave") ? new DurationRange(4.5, 7.0) : new DurationRange(6.0, 12.0); }
    }

    private enum Motion {
        ORBIT, TIGHT_ORBIT, CROWN_ORBIT, BREATHING_ORBIT, HELICAL_PORTRAIT, ISOMETRIC_HOLD,
        HERO_SWEEP, FIGURE_EIGHT, CRANE, PARALLAX, EXPEDITION, TERRAIN_SCOUT, TWO_SHOT, CELESTIAL;

        CameraMotion create(CinematicContext context, CinematicSubject subject, RandomGenerator random) {
            double seedAngle = random.nextDouble(360.0);
            Vec3 player = context.player().focus();
            Vec3 subjectFocus = subject.focus();
            return (progress, elapsed) -> {
                double p = clamp(progress);
                double angle;
                double distance;
                double elevation;
                double lateral = 0.0;
                double vertical = 0.0;
                CinematicRigState.YawMode yawMode = CinematicRigState.YawMode.SHORTEST_PATH;
                switch (this) {
                    case ORBIT -> { angle = seedAngle + elapsed * 10.0; distance = 5.4; elevation = 17.0; yawMode = CinematicRigState.YawMode.FORWARD_ONLY; }
                    case TIGHT_ORBIT -> { angle = seedAngle + elapsed * 8.0; distance = 3.0; elevation = 13.0; yawMode = CinematicRigState.YawMode.FORWARD_ONLY; }
                    case CROWN_ORBIT -> { angle = seedAngle + elapsed * 7.0; distance = 8.5; elevation = 28.0 + Math.sin(p * Math.PI) * 7.0; yawMode = CinematicRigState.YawMode.FORWARD_ONLY; }
                    case BREATHING_ORBIT -> { angle = seedAngle + elapsed * 9.0; distance = 5.0 + Math.sin(elapsed * 0.65) * 0.65; elevation = 18.0 + Math.sin(elapsed * 0.42) * 5.0; yawMode = CinematicRigState.YawMode.FORWARD_ONLY; }
                    case HELICAL_PORTRAIT -> { angle = seedAngle + p * 42.0; distance = 3.4; elevation = 5.0 + p * 22.0; }
                    case ISOMETRIC_HOLD -> { angle = seedAngle + (p - 0.5) * 5.0; distance = 5.2; elevation = 25.0; lateral = Math.sin(p * Math.PI * 2.0) * 0.25; }
                    case HERO_SWEEP -> { angle = seedAngle + p * 48.0; distance = 4.4; elevation = -7.0 + p * 6.0; }
                    case FIGURE_EIGHT -> { angle = seedAngle + p * 360.0; distance = 5.0; elevation = 14.0 + Math.sin(p * Math.PI * 4.0) * 8.0; lateral = Math.sin(p * Math.PI * 2.0) * 1.25; yawMode = CinematicRigState.YawMode.FORWARD_ONLY; }
                    case CRANE -> { angle = seedAngle + p * 12.0; distance = 6.5; elevation = 6.0 + p * 39.0; vertical = p * 2.0; }
                    case PARALLAX -> { angle = directionAngle(context) + 180.0; distance = 5.5; elevation = 10.0; lateral = (p - 0.5) * 5.0; }
                    case EXPEDITION -> { angle = seedAngle + (p < 0.4 ? p * 15.0 : 6.0 + (p - 0.4) * 38.0); distance = p < 0.4 ? 7.5 - p * 7.0 : 4.7 + (p - 0.4) * 2.0; elevation = 12.0 + p * 12.0; }
                    case TERRAIN_SCOUT -> { angle = directionAngle(context) + 155.0 + p * 18.0; distance = 7.0; elevation = 22.0; lateral = (p - 0.5) * 2.0; }
                    case TWO_SHOT -> { angle = seedAngle + p * 18.0; distance = Math.max(4.5, player.distanceTo(subjectFocus) * 0.75 + 3.0); elevation = 14.0; }
                    case CELESTIAL -> { angle = directionAngle(context) + 180.0 + p * 8.0; distance = 7.0; elevation = 28.0; }
                    default -> throw new IllegalStateException("Unknown motion " + this);
                }
                Vec3 anchor = this == TWO_SHOT ? player.lerp(subjectFocus, 0.5) : (tagsEnvironment(this) ? player : subjectFocus);
                Vec3 focus = subjectFocus;
                return new CinematicRigState(anchor, focus, distance, angle, elevation, lateral, vertical, 0.0,
                        OptionalDouble.empty(), subject, yawMode);
            };
        }

        private static boolean tagsEnvironment(Motion motion) { return motion == PARALLAX || motion == TERRAIN_SCOUT || motion == CELESTIAL; }
        private static double directionAngle(CinematicContext context) {
            Vec3 direction = context.mostOpenDirection().map(CinematicContext.DirectionalProbe::direction).orElse(new Vec3(1, 0, 0));
            return Math.toDegrees(Math.atan2(direction.z, direction.x));
        }
        private static double clamp(double value) { return Math.max(0.0, Math.min(1.0, Double.isFinite(value) ? value : 0.0)); }
    }
}
