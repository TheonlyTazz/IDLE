package dev.theonlytazz.idlecinematics.client;

import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.random.RandomGenerator;

public final class BuiltInPresets {
    private BuiltInPresets() {}

    public static List<ShotPreset> create() {
        return List.of(
                preset("orbit", tags(ShotTag.PLAYER), scene -> 1.0, playerFocus(),
                        (t, a) -> radial(a, 5.4, 1.7 + Math.sin(a * 0.6) * 0.35)),
                preset("tight_orbit", tags(ShotTag.PLAYER, ShotTag.CLOSE), scene -> scene.openArea() ? 0.8 : 1.3, playerFocus(),
                        (t, a) -> radial(a, 3.25, 1.15 + Math.sin(a) * 0.25)),
                preset("wide_orbit", tags(ShotTag.PLAYER, ShotTag.WIDE, ShotTag.OPEN_SKY), scene -> scene.openArea() ? 1.4 : 0.0, playerFocus(),
                        (t, a) -> radial(a, 9.0, 3.8 + Math.sin(t * Math.PI) * 1.2)),
                preset("hero_low", tags(ShotTag.PLAYER), scene -> 1.0, playerFocus(),
                        (t, a) -> radial(a, 4.2, -0.45 + Math.sin(t * Math.PI) * 0.45)),
                preset("profile", tags(ShotTag.PLAYER), scene -> 1.0, playerFocus(), (t, a) -> radial(a, 3.8, 1.15)),
                preset("over_shoulder", tags(ShotTag.PLAYER, ShotTag.CLOSE), scene -> 1.0, playerFocus(), (t, a) -> radial(a, 2.25, 1.55)),
                preset("overhead", tags(ShotTag.PLAYER), scene -> scene.openDirections() >= 2 ? 1.0 : 0.25, playerFocus(),
                        (t, a) -> radial(a, 2.6, 7.5)),
                preset("push_in", tags(ShotTag.PLAYER), scene -> 1.2, playerFocus(),
                        (t, a) -> radial(a, 5.8 - t * 2.6, 1.35)),
                preset("side_slide", tags(ShotTag.PLAYER), scene -> 1.1, playerFocus(), (t, a) -> {
                    double side = Math.sin(a), forward = Math.cos(a), slide = (t - 0.5) * 3.0;
                    return new Vec3(forward * 4.0 + side * slide, 1.65, side * 4.0 - forward * slide);
                }),
                preset("skyline", tags(ShotTag.ENVIRONMENT, ShotTag.WIDE, ShotTag.OPEN_SKY), scene -> scene.openArea() ? 1.7 : 0.0,
                        scene -> scene.playerFocus().add(0, 2.5, 0), (t, a) -> radial(a, 11.5, 5.5 + t * 1.5)),
                preset("golden_hour", tags(ShotTag.ENVIRONMENT, ShotTag.WIDE, ShotTag.OPEN_SKY, ShotTag.GOLDEN_HOUR),
                        scene -> scene.dayPhase() == SceneContext.DayPhase.SUNRISE || scene.dayPhase() == SceneContext.DayPhase.SUNSET ? 3.0 : 0.15,
                        scene -> scene.playerFocus().add(0, 1.5, 0), (t, a) -> radial(a, 8.5, 2.8 + Math.sin(t * Math.PI) * 0.8)),
                preset("reveal", tags(ShotTag.ENVIRONMENT, ShotTag.WIDE, ShotTag.OPEN_SKY), scene -> scene.openArea() ? 1.5 : 0.0,
                        playerFocus(), (t, a) -> radial(a, 3.5 + t * 8.0, 1.0 + t * 5.0)),
                preset("night_sky", tags(ShotTag.ENVIRONMENT, ShotTag.OPEN_SKY, ShotTag.NIGHT),
                        scene -> scene.dayPhase() == SceneContext.DayPhase.NIGHT ? 2.6 : 0.0,
                        scene -> scene.playerFocus().add(0, 4.0, 0), (t, a) -> radial(a, 7.5, 5.5 + t)),
                preset("nether_ridge", tags(ShotTag.ENVIRONMENT, ShotTag.WIDE, ShotTag.NETHER),
                        scene -> scene.dimension() == SceneContext.DimensionKind.NETHER && scene.openArea() ? 2.4 : 0.0,
                        playerFocus(), (t, a) -> radial(a, 8.0, 3.0 + Math.sin(t * Math.PI) * 1.5)),
                preset("nether_chasm", tags(ShotTag.ENVIRONMENT, ShotTag.NETHER),
                        scene -> scene.dimension() == SceneContext.DimensionKind.NETHER ? 1.8 : 0.0,
                        scene -> scene.playerFocus().add(0, -1.0, 0), (t, a) -> radial(a, 5.0 + t * 2.0, 2.5)),
                preset("end_spire", tags(ShotTag.ENVIRONMENT, ShotTag.WIDE, ShotTag.END),
                        scene -> scene.dimension() == SceneContext.DimensionKind.END && scene.openArea() ? 2.5 : 0.0,
                        scene -> scene.playerFocus().add(0, 3.0, 0), (t, a) -> radial(a, 10.0, 5.0)),
                preset("end_void", tags(ShotTag.ENVIRONMENT, ShotTag.END),
                        scene -> scene.dimension() == SceneContext.DimensionKind.END ? 1.8 : 0.0,
                        playerFocus(), (t, a) -> radial(a, 6.5 + t * 2.0, 1.5 + t * 2.0)),
                cave("cave_track", scene -> 1.2, (t, a) -> radial(a, 3.2, 0.75 + Math.sin(t * Math.PI) * 0.5)),
                cave("cave_close", scene -> scene.ceilingClearance() < 5.0 ? 2.2 : 1.4,
                        (t, a) -> radial(a, 2.35, 0.65 + Math.sin(t * Math.PI) * 0.25)),
                cave("cave_shoulder", scene -> 1.8, (t, a) -> radial(a, 1.8, 1.3)),
                cave("cave_arc", scene -> scene.openDirections() >= 2 ? 1.5 : 0.8, (t, a) -> radial(a, 2.7, 1.05)),
                cave("cave_low", scene -> 1.2, (t, a) -> radial(a, 2.6, -0.15 + Math.sin(t * Math.PI) * 0.3)),
                preset("companion", tags(ShotTag.ENVIRONMENT, ShotTag.ENTITY), scene -> scene.nearbyEntityFocus().isPresent() ? 2.0 : 0.0,
                        scene -> scene.nearbyEntityFocus().orElse(scene.playerFocus()), (t, a) -> radial(a, 6.0, 2.25))
        );
    }

    private static ShotPreset cave(String id, ToDoubleFunction<SceneContext> weight, CameraPath path) {
        return preset(id, tags(ShotTag.PLAYER, ShotTag.CAVE, ShotTag.CLOSE), weight, playerFocus(), path);
    }

    private static ShotPreset preset(String id, Set<ShotTag> tags, ToDoubleFunction<SceneContext> weight,
                                     Function<SceneContext, Vec3> focus, CameraPath path) {
        return new ParametricPreset(id, tags, weight, focus, path);
    }

    private static Set<ShotTag> tags(ShotTag... tags) {
        return Set.of(tags);
    }

    private static Function<SceneContext, Vec3> playerFocus() {
        return SceneContext::playerFocus;
    }

    private static Vec3 radial(double angle, double radius, double height) {
        return new Vec3(Math.cos(angle) * radius, height, Math.sin(angle) * radius);
    }

    private record ParametricPreset(String id, Set<ShotTag> tags, ToDoubleFunction<SceneContext> weight,
                                    Function<SceneContext, Vec3> focus, CameraPath path) implements ShotPreset {
        @Override
        public double contextWeight(SceneContext scene) {
            return Math.max(0.0, weight.applyAsDouble(scene));
        }

        @Override
        public ShotPlan createPlan(SceneContext scene, RandomGenerator random, int configuredDurationTicks) {
            double durationScale = 0.72 + random.nextDouble() * 0.28;
            int duration = tags.contains(ShotTag.CAVE) ? random.nextInt(90, 131)
                    : Math.max(80, (int) Math.round(configuredDurationTicks * durationScale));
            int transition = Math.min(36, Math.max(18, duration / 5));
            return new ShotPlan(id, focus.apply(scene), path, duration, transition);
        }
    }
}
