package dev.theonlytazz.idlecinematics.api;

import dev.theonlytazz.idlecinematics.core.NamespacedId;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.random.RandomGenerator;
import static org.junit.jupiter.api.Assertions.*;

final class CinematicPresetsTest {
    @Test void acceptsConstructorAndEventRegistrationThenFreezesExactlyOnce() {
        CinematicPresets.register(preset("constructor_scene"));
        AtomicInteger events = new AtomicInteger();
        CinematicPresets.completeRegistration(event -> {
            events.incrementAndGet();
            event.register(preset("event_scene"));
        });
        CinematicPresets.completeRegistration(event -> events.incrementAndGet());
        assertEquals(1, events.get());
        assertFalse(CinematicPresets.isRegistrationOpen());
        assertThrows(IllegalStateException.class, () -> CinematicPresets.register(preset("too_late")));
        assertThrows(IllegalArgumentException.class, () -> {
            throw duplicateRegistrationFailure();
        });
    }

    private static IllegalArgumentException duplicateRegistrationFailure() {
        var registry = dev.theonlytazz.idlecinematics.client.shots.ShotRegistry.createBuiltIns();
        registry.register(preset("duplicate"));
        try { registry.register(preset("duplicate")); }
        catch (IllegalArgumentException exception) { return exception; }
        throw new AssertionError("duplicate registration unexpectedly succeeded");
    }

    private static CinematicPreset preset(String path) {
        return new CinematicPreset() {
            @Override public NamespacedId id() { return new NamespacedId("addon_test", path); }
            @Override public String pool() { return "landscape"; }
            @Override public Set<String> tags() { return Set.of("environment"); }
            @Override public double contextScore(CinematicContext context) { return 1.0; }
            @Override public CinematicSubject selectSubject(CinematicContext context, RandomGenerator random) { return context.player(); }
            @Override public CameraMotion createMotion(CinematicContext context, CinematicSubject subject, RandomGenerator random) {
                return (progress, elapsed) -> new CinematicRigState(Vec3.ZERO, subject.focus(), 4.0, 0.0, 12.0,
                        0.0, 0.0, 0.0, OptionalDouble.empty(), subject, CinematicRigState.YawMode.SHORTEST_PATH);
            }
            @Override public TransitionSpec transition() { return TransitionSpec.damped(0.5); }
            @Override public SafetyPolicy safety() { return SafetyPolicy.standard(); }
            @Override public DurationRange duration() { return new DurationRange(6.0, 8.0); }
        };
    }
}
