package dev.theonlytazz.idlecinematics.api;

import dev.theonlytazz.idlecinematics.core.NamespacedId;
import java.util.Set;
import java.util.random.RandomGenerator;

public interface CinematicPreset {
    int API_VERSION = 1;
    NamespacedId id();
    String pool();
    Set<String> tags();
    double contextScore(CinematicContext context);
    CinematicSubject selectSubject(CinematicContext context, RandomGenerator random);
    CameraMotion createMotion(CinematicContext context, CinematicSubject subject, RandomGenerator random);
    TransitionSpec transition();
    SafetyPolicy safety();
    DurationRange duration();

    record DurationRange(double minimumSeconds, double maximumSeconds) {
        public DurationRange {
            minimumSeconds = Math.max(1.0, minimumSeconds);
            maximumSeconds = Math.max(minimumSeconds, maximumSeconds);
        }
        public double sample(RandomGenerator random) { return minimumSeconds + random.nextDouble() * (maximumSeconds - minimumSeconds); }
    }
}
