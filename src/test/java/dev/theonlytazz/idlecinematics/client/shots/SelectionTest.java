package dev.theonlytazz.idlecinematics.client.shots;

import dev.theonlytazz.idlecinematics.TestContexts;
import dev.theonlytazz.idlecinematics.api.*;
import dev.theonlytazz.idlecinematics.config.ClientConfig;
import dev.theonlytazz.idlecinematics.core.NamespacedId;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

final class SelectionTest {
    @Test void selectionIsDeterministicAndPenalizesImmediatePresetReuse() {
        ShotRegistry registry = new ShotRegistry();
        registry.register(simple("one", 1)); registry.register(simple("two", 1)); registry.freeze();
        CinematicContext context = TestContexts.context(false, CinematicContext.DimensionKind.OVERWORLD,
                CinematicContext.DayPhase.DAY, CinematicContext.Weather.CLEAR, false, 12, Optional.empty());
        ShotDirector left = new ShotDirector(registry, new Random(42));
        ShotDirector right = new ShotDirector(registry, new Random(42));
        String leftFirst = left.next(context, ClientConfig.ShotMode.PLAYER_FOCUSED, 200).presetId();
        String rightFirst = right.next(context, ClientConfig.ShotMode.PLAYER_FOCUSED, 200).presetId();
        assertEquals(leftFirst, rightFirst);
        String second = left.next(context, ClientConfig.ShotMode.PLAYER_FOCUSED, 200).presetId();
        assertNotEquals(leftFirst, second);
    }

    @Test void boundedReselectionUsesExactlyThreeAttemptsThenFallback() {
        AtomicInteger calls = new AtomicInteger();
        String selected = BoundedShotSelector.choose(3, () -> "bad" + calls.incrementAndGet(), value -> false, () -> "fallback");
        assertEquals("fallback", selected); assertEquals(3, calls.get());
    }

    @Test void builtInsContainCorrectCaveFallbackAndContextConstraints() {
        ShotRegistry registry = ShotRegistry.createBuiltIns();
        assertNotNull(registry.require(new NamespacedId("idlecinematics", "cave_close_portrait")));
        assertThrows(IllegalStateException.class, () -> registry.require(new NamespacedId("idlecinematics", "cave_close")));
        CinematicContext clear = TestContexts.context(false, CinematicContext.DimensionKind.OVERWORLD,
                CinematicContext.DayPhase.DAY, CinematicContext.Weather.CLEAR, true, 12, Optional.empty());
        CinematicContext rain = TestContexts.context(false, CinematicContext.DimensionKind.OVERWORLD,
                CinematicContext.DayPhase.DAY, CinematicContext.Weather.RAIN, true, 12, Optional.empty());
        CinematicPreset storm = registry.require(new NamespacedId("idlecinematics", "rain_silhouette"));
        assertEquals(0, storm.contextScore(clear)); assertTrue(storm.contextScore(rain) > 0);
        CinematicContext shortView = TestContexts.context(false, CinematicContext.DimensionKind.END,
                CinematicContext.DayPhase.NIGHT, CinematicContext.Weather.CLEAR, true, 4, Optional.empty());
        CinematicPreset wide = registry.require(new NamespacedId("idlecinematics", "wide_orbit"));
        assertEquals(0, wide.contextScore(shortView));
    }

    @Test void registryRejectsDuplicatesAndBecomesImmutable() {
        ShotRegistry registry = new ShotRegistry(); registry.register(simple("one", 1));
        assertThrows(IllegalArgumentException.class, () -> registry.register(simple("one", 2)));
        registry.freeze(); assertThrows(IllegalStateException.class, () -> registry.register(simple("two", 1)));
    }

    @Test void invalidEntitiesFallBackWithoutThrowing() {
        CinematicSubject fallback = new CinematicSubject(CinematicSubject.Type.WORLD_POSITION, Optional.empty(), Vec3.ZERO, 0, 0);
        CinematicSubject entity = new CinematicSubject(CinematicSubject.Type.ENTITY, Optional.of(new java.util.UUID(0, 3)), new Vec3(4, 0, 0), 1, 0);
        SubjectSelector selector = new SubjectSelector();
        assertSame(fallback, selector.select(java.util.List.of(new SubjectSelector.Candidate(entity, false, 1)),
                fallback, Vec3.ZERO, candidate -> true, new Random(1)));
    }

    private static CinematicPreset simple(String id, double score) {
        return new CinematicPreset() {
            private final NamespacedId value = new NamespacedId("test", id);
            @Override public NamespacedId id() { return value; }
            @Override public String pool() { return "player"; }
            @Override public Set<String> tags() { return Set.of("player"); }
            @Override public double contextScore(CinematicContext context) { return score; }
            @Override public CinematicSubject selectSubject(CinematicContext context, java.util.random.RandomGenerator random) { return context.player(); }
            @Override public CameraMotion createMotion(CinematicContext context, CinematicSubject subject, java.util.random.RandomGenerator random) {
                return (progress, elapsed) -> new CinematicRigState(Vec3.ZERO, Vec3.ZERO, 3, 0, 0, 0, 0, 0,
                        java.util.OptionalDouble.empty(), subject, CinematicRigState.YawMode.SHORTEST_PATH);
            }
            @Override public TransitionSpec transition() { return TransitionSpec.cut(); }
            @Override public SafetyPolicy safety() { return SafetyPolicy.standard(); }
            @Override public DurationRange duration() { return new DurationRange(5, 5); }
        };
    }
}
