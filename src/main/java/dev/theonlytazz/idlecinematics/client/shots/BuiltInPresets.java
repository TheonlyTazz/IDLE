package dev.theonlytazz.idlecinematics.client.shots;

import dev.theonlytazz.idlecinematics.client.camera.CameraPath;
import dev.theonlytazz.idlecinematics.client.scene.SceneContext;

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
                shot("orbit", ShotPool.PLAYER, tags(ShotTag.PLAYER), any(), player(), player(), orbit(5.4, 1.7, 0.35)),
                shot("tight_orbit", ShotPool.PLAYER, tags(ShotTag.PLAYER, ShotTag.CLOSE), any(), player(), player(), orbit(3.2, 1.15, 0.2)),
                shot("wide_orbit", ShotPool.PLAYER, tags(ShotTag.PLAYER, ShotTag.WIDE, ShotTag.OPEN_SKY), open(1.4), player(), player(), orbit(9.0, 3.8, 0.8)),
                shot("hero_low", ShotPool.PLAYER, tags(ShotTag.PLAYER), any(), player(), player(), locked(4.2, -0.35, 0.6)),
                shot("profile", ShotPool.PLAYER, tags(ShotTag.PLAYER), any(), player(), player(), locked(3.8, 1.1, 1.2)),
                shot("over_shoulder", ShotPool.PLAYER, tags(ShotTag.PLAYER, ShotTag.CLOSE), any(), player(), player(), locked(2.25, 1.5, 0.5)),
                shot("overhead", ShotPool.PLAYER, tags(ShotTag.PLAYER), scene -> scene.ceilingClearance() > 8 ? 1.2 : 0.0, player(), player(), locked(2.4, 7.0, 0.4)),
                shot("push_in", ShotPool.PLAYER, tags(ShotTag.PLAYER), any(), player(), player(), push(6.0, 3.0, 1.35)),
                shot("side_slide", ShotPool.PLAYER, tags(ShotTag.PLAYER), any(), player(), player(), slide(4.0, 1.6, 3.5)),

                shot("landscape_reveal", ShotPool.LANDSCAPE, tags(ShotTag.ENVIRONMENT, ShotTag.WIDE, ShotTag.OPEN_SKY), open(1.8), player(), openFocus(12, 2), push(4.0, 11.0, 4.0)),
                shot("landscape_crosspan", ShotPool.LANDSCAPE, tags(ShotTag.ENVIRONMENT, ShotTag.WIDE), open(1.5), player(), openFocus(14, 1), slide(8.0, 3.5, 8.0)),
                shot("landscape_overlook", ShotPool.LANDSCAPE, tags(ShotTag.ENVIRONMENT, ShotTag.WIDE), open(1.4), player(), openFocus(16, 0), locked(10.0, 7.0, 1.0)),

                shot("entity_two_shot", ShotPool.ENTITY, tags(ShotTag.ENVIRONMENT, ShotTag.ENTITY), entity(), player(), entityFocus(), locked(5.0, 1.8, 1.0)),
                shot("entity_push", ShotPool.ENTITY, tags(ShotTag.ENVIRONMENT, ShotTag.ENTITY), entity(), player(), entityFocus(), push(7.0, 3.5, 1.4)),
                shot("entity_watch", ShotPool.ENTITY, tags(ShotTag.ENVIRONMENT, ShotTag.ENTITY), entity(), player(), entityFocus(), slide(5.5, 2.2, 4.0)),

                shot("cave_passage", ShotPool.CAVE, tags(ShotTag.PLAYER, ShotTag.CAVE, ShotTag.CLOSE), cave(), player(), openFocus(6, 0), push(3.2, 2.0, 0.8)),
                shot("cave_wall_detail", ShotPool.CAVE, tags(ShotTag.ENVIRONMENT, ShotTag.CAVE, ShotTag.CLOSE), cave(), player(), wallFocus(), locked(2.4, 0.7, 0.5)),
                shot("cave_floor_drift", ShotPool.CAVE, tags(ShotTag.ENVIRONMENT, ShotTag.CAVE, ShotTag.CLOSE), cave(), player(), floorFocus(), slide(2.7, 0.35, 2.0)),
                shot("cave_ceiling_arc", ShotPool.CAVE, tags(ShotTag.ENVIRONMENT, ShotTag.CAVE, ShotTag.CLOSE), cave(), player(), ceilingFocus(), orbit(2.6, 1.3, 0.25)),
                shot("cave_close_portrait", ShotPool.CAVE, tags(ShotTag.PLAYER, ShotTag.CAVE, ShotTag.CLOSE), cave(), player(), player(), locked(1.9, 1.1, 0.4)),

                shot("nether_ridge", ShotPool.NETHER, tags(ShotTag.ENVIRONMENT, ShotTag.NETHER, ShotTag.WIDE), dimension(SceneContext.DimensionKind.NETHER), player(), openFocus(14, 2), slide(8.0, 3.0, 7.0)),
                shot("nether_chasm", ShotPool.NETHER, tags(ShotTag.ENVIRONMENT, ShotTag.NETHER), dimension(SceneContext.DimensionKind.NETHER), player(), floorFocus(), locked(6.0, 4.0, 1.0)),
                shot("nether_passage", ShotPool.NETHER, tags(ShotTag.PLAYER, ShotTag.NETHER), dimension(SceneContext.DimensionKind.NETHER), player(), openFocus(8, 0), push(5.0, 2.6, 1.2)),

                shot("end_spire", ShotPool.END, tags(ShotTag.ENVIRONMENT, ShotTag.END, ShotTag.WIDE), dimension(SceneContext.DimensionKind.END), player(), openFocus(16, 5), locked(10.0, 6.0, 1.0)),
                shot("end_void_edge", ShotPool.END, tags(ShotTag.ENVIRONMENT, ShotTag.END), dimension(SceneContext.DimensionKind.END), player(), floorFocus(), slide(7.0, 2.0, 6.0)),
                shot("end_gateway_drift", ShotPool.END, tags(ShotTag.PLAYER, ShotTag.END), dimension(SceneContext.DimensionKind.END), player(), player(), orbit(7.5, 3.5, 0.3)),

                sky("sunrise_horizon", ShotPool.SUNRISE, SceneContext.DayPhase.SUNRISE, locked(8.0, 3.0, 1.0)),
                sky("sunrise_rise", ShotPool.SUNRISE, SceneContext.DayPhase.SUNRISE, crane(7.0, 1.5, 6.5)),
                sky("day_high_sky", ShotPool.DAY, SceneContext.DayPhase.DAY, locked(7.0, 5.5, 1.0)),
                sky("day_landscape_pan", ShotPool.DAY, SceneContext.DayPhase.DAY, slide(9.0, 4.0, 8.0)),
                sky("sunset_rim", ShotPool.SUNSET, SceneContext.DayPhase.SUNSET, locked(6.5, 2.0, 1.0)),
                sky("sunset_long_pan", ShotPool.SUNSET, SceneContext.DayPhase.SUNSET, slide(10.0, 3.0, 9.0)),
                sky("night_stars", ShotPool.NIGHT, SceneContext.DayPhase.NIGHT, locked(6.0, 5.0, 1.0)),
                sky("night_moonline", ShotPool.NIGHT, SceneContext.DayPhase.NIGHT, crane(8.0, 2.0, 6.0))
        );
    }

    private static ShotPreset sky(String id, ShotPool pool, SceneContext.DayPhase phase, PathFactory path) {
        return shot(id, pool, tags(ShotTag.ENVIRONMENT, ShotTag.OPEN_SKY),
                scene -> scene.openSky() && scene.dayPhase() == phase ? 2.5 : 0.0, player(), SceneContext::celestialTarget, path);
    }

    private static ShotPreset shot(String id, ShotPool pool, Set<ShotTag> tags, ToDoubleFunction<SceneContext> weight,
                                   Function<SceneContext, Vec3> anchor, Function<SceneContext, Vec3> focus, PathFactory path) {
        return new ParametricPreset(id, pool, tags, weight, anchor, focus, path);
    }

    private static ToDoubleFunction<SceneContext> any() { return scene -> 1.0; }
    private static ToDoubleFunction<SceneContext> open(double weight) { return scene -> scene.openArea() ? weight : 0.0; }
    private static ToDoubleFunction<SceneContext> entity() { return scene -> scene.nearbyEntityFocus().isPresent() ? 2.0 : 0.0; }
    private static ToDoubleFunction<SceneContext> cave() { return scene -> scene.enclosed() ? 2.0 : 0.0; }
    private static ToDoubleFunction<SceneContext> dimension(SceneContext.DimensionKind kind) { return scene -> scene.dimension() == kind ? 2.2 : 0.0; }
    private static Function<SceneContext, Vec3> player() { return SceneContext::playerFocus; }
    private static Function<SceneContext, Vec3> entityFocus() { return scene -> scene.nearbyEntityFocus().orElse(scene.playerFocus()); }
    private static Function<SceneContext, Vec3> wallFocus() { return scene -> scene.wallTarget().orElse(scene.playerFocus()); }
    private static Function<SceneContext, Vec3> floorFocus() { return scene -> scene.floorTarget().orElse(scene.playerFocus().add(0, -1, 0)); }
    private static Function<SceneContext, Vec3> ceilingFocus() { return scene -> scene.ceilingTarget().orElse(scene.playerFocus().add(0, 2, 0)); }
    private static Function<SceneContext, Vec3> openFocus(double distance, double height) {
        return scene -> scene.playerFocus().add(scene.openDirection().scale(distance)).add(0, height, 0);
    }
    private static Set<ShotTag> tags(ShotTag... tags) { return Set.of(tags); }

    private static PathFactory orbit(double radius, double height, double bob) {
        return (scene, random) -> (t, phase) -> radial(phase, radius, height + Math.sin(t * Math.PI * 2) * bob);
    }
    private static PathFactory locked(double radius, double height, double drift) {
        return (scene, random) -> { double angle = random.nextDouble(Math.PI * 2); return (t, phase) -> radial(angle + (t - 0.5) * drift, radius, height); };
    }
    private static PathFactory push(double from, double to, double height) {
        return (scene, random) -> { double angle = random.nextDouble(Math.PI * 2); return (t, phase) -> radial(angle, from + (to - from) * t, height); };
    }
    private static PathFactory slide(double radius, double height, double width) {
        return (scene, random) -> { double angle = random.nextDouble(Math.PI * 2); return (t, phase) -> {
            Vec3 radial = radial(angle, radius, height); Vec3 tangent = new Vec3(-Math.sin(angle), 0, Math.cos(angle));
            return radial.add(tangent.scale((t - 0.5) * width));
        }; };
    }
    private static PathFactory crane(double radius, double low, double high) {
        return (scene, random) -> { double angle = random.nextDouble(Math.PI * 2); return (t, phase) -> radial(angle, radius, low + (high - low) * t); };
    }
    private static Vec3 radial(double angle, double radius, double height) { return new Vec3(Math.cos(angle) * radius, height, Math.sin(angle) * radius); }

    @FunctionalInterface
    private interface PathFactory { CameraPath create(SceneContext scene, RandomGenerator random); }

    private record ParametricPreset(String id, ShotPool pool, Set<ShotTag> tags, ToDoubleFunction<SceneContext> weight,
                                    Function<SceneContext, Vec3> anchor, Function<SceneContext, Vec3> focus,
                                    PathFactory path) implements ShotPreset {
        @Override public double contextWeight(SceneContext scene) { return Math.max(0.0, weight.applyAsDouble(scene)); }
        @Override public ShotPlan createPlan(SceneContext scene, RandomGenerator random, int configuredDurationTicks) {
            double durationScale = 0.72 + random.nextDouble() * 0.28;
            int duration = pool == ShotPool.CAVE ? random.nextInt(90, 131) : Math.max(80, (int) Math.round(configuredDurationTicks * durationScale));
            return new ShotPlan(id, pool, anchor.apply(scene), focus.apply(scene), path.create(scene, random), duration,
                    Math.min(36, Math.max(18, duration / 5)));
        }
    }
}
