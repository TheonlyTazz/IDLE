package dev.theonlytazz.idlecinematics.client.landmark;

import dev.theonlytazz.idlecinematics.api.CameraMotion;
import dev.theonlytazz.idlecinematics.api.CinematicContext;
import dev.theonlytazz.idlecinematics.api.CinematicPreset;
import dev.theonlytazz.idlecinematics.api.CinematicRigState;
import dev.theonlytazz.idlecinematics.api.CinematicSubject;
import dev.theonlytazz.idlecinematics.api.SafetyPolicy;
import dev.theonlytazz.idlecinematics.api.TransitionSpec;
import dev.theonlytazz.idlecinematics.core.NamespacedId;

import java.util.OptionalDouble;
import java.util.Set;
import java.util.random.RandomGenerator;

/** Immutable parameterized landmark scene used by scripting integrations. */
public record LandmarkScenePreset(NamespacedId id, NamespacedId landmarkId, Style style, double weight,
                                  double distanceScale, double angularSpeed, double elevation,
                                  DurationRange duration, TransitionSpec transition,
                                  OptionalDouble cinematicFov) implements CinematicPreset {
    public LandmarkScenePreset {
        if (!Double.isFinite(weight) || weight <= 0.0) throw new IllegalArgumentException("weight must be finite and positive");
        if (!Double.isFinite(distanceScale) || distanceScale <= 0.0) throw new IllegalArgumentException("distanceScale must be finite and positive");
        if (!Double.isFinite(angularSpeed)) throw new IllegalArgumentException("angularSpeed must be finite");
        if (!Double.isFinite(elevation)) throw new IllegalArgumentException("elevation must be finite");
        if (cinematicFov.isPresent() && (!Double.isFinite(cinematicFov.getAsDouble())
                || cinematicFov.getAsDouble() < 1.0 || cinematicFov.getAsDouble() > 179.0)) {
            throw new IllegalArgumentException("cinematicFov must be between 1 and 179 degrees");
        }
    }

    @Override public String pool() { return "landmark"; }
    @Override public Set<String> tags() { return Set.of("environment", "landmark", "scripted"); }

    @Override
    public double contextScore(CinematicContext context) {
        return context.selectedLandmark().filter(value -> value.typeId().equals(landmarkId))
                .map(value -> value.score() * weight).orElse(0.0);
    }

    @Override
    public CinematicSubject selectSubject(CinematicContext context, RandomGenerator random) {
        return context.selectedLandmark().filter(value -> value.typeId().equals(landmarkId))
                .map(dev.theonlytazz.idlecinematics.api.CinematicLandmark::subject).orElse(context.player());
    }

    @Override
    public CameraMotion createMotion(CinematicContext context, CinematicSubject subject, RandomGenerator random) {
        double initialAngle = random.nextDouble(360.0);
        double framingDistance = Math.max(3.5, Math.min(18.0, subject.size() * 0.5 * distanceScale + 2.5));
        return (progress, elapsedSeconds) -> {
            double p = Math.max(0.0, Math.min(1.0, Double.isFinite(progress) ? progress : 0.0));
            double angle;
            double distance = framingDistance;
            double pitch = elevation;
            double lateral = 0.0;
            double vertical = 0.0;
            CinematicRigState.YawMode yawMode = CinematicRigState.YawMode.SHORTEST_PATH;
            switch (style) {
                case ORBIT -> {
                    angle = initialAngle + elapsedSeconds * angularSpeed;
                    yawMode = angularSpeed >= 0.0 ? CinematicRigState.YawMode.FORWARD_ONLY
                            : CinematicRigState.YawMode.SHORTEST_PATH;
                }
                case REVEAL -> {
                    angle = initialAngle + p * angularSpeed;
                    distance = Math.max(2.0, framingDistance + 1.5 - p * 1.5);
                    pitch = elevation + p * 10.0;
                    lateral = (p - 0.5) * 1.5;
                }
                case CRANE -> {
                    angle = initialAngle + p * angularSpeed;
                    pitch = elevation + p * 38.0;
                    vertical = p * Math.min(3.5, subject.size() * 0.3);
                }
                case HOLD -> {
                    angle = initialAngle + Math.sin(p * Math.PI * 2.0) * angularSpeed;
                    lateral = Math.sin(p * Math.PI * 2.0) * 0.35;
                }
                default -> throw new IllegalStateException("Unknown landmark scene style " + style);
            }
            return new CinematicRigState(subject.focus(), subject.focus(), distance, angle, pitch, lateral,
                    vertical, 0.0, cinematicFov, subject, yawMode);
        };
    }

    @Override public SafetyPolicy safety() {
        return new SafetyPolicy(1.5, 20.0, -72.0, 78.0, 0.28,
                SafetyPolicy.FluidPolicy.ALLOW_WATER, 0.2);
    }

    public enum Style { ORBIT, REVEAL, CRANE, HOLD }
}
