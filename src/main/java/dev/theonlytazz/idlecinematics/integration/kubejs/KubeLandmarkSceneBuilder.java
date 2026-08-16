package dev.theonlytazz.idlecinematics.integration.kubejs;

import dev.theonlytazz.idlecinematics.api.CinematicPreset;
import dev.theonlytazz.idlecinematics.api.TransitionSpec;
import dev.theonlytazz.idlecinematics.client.landmark.LandmarkScenePreset;
import dev.theonlytazz.idlecinematics.core.NamespacedId;

import java.util.Locale;
import java.util.OptionalDouble;

public final class KubeLandmarkSceneBuilder {
    private final String id;
    private final String landmarkId;
    private final LandmarkScenePreset.Style style;
    private double weight = 1.0;
    private double distanceScale = 1.4;
    private double speed = 8.0;
    private double elevation = 18.0;
    private double minimumDuration = 6.0;
    private double maximumDuration = 10.0;
    private TransitionSpec transition = TransitionSpec.damped(0.7);
    private OptionalDouble fov = OptionalDouble.empty();

    KubeLandmarkSceneBuilder(String id, String landmarkId, String style) {
        this.id = id;
        this.landmarkId = landmarkId;
        try {
            this.style = LandmarkScenePreset.Style.valueOf(style.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown landmark scene style '" + style
                    + "'; expected orbit, reveal, crane, or hold", exception);
        }
    }

    public KubeLandmarkSceneBuilder weight(double value) { weight = value; return this; }
    public KubeLandmarkSceneBuilder distance(double value) { distanceScale = value; return this; }
    public KubeLandmarkSceneBuilder speed(double value) { speed = value; return this; }
    public KubeLandmarkSceneBuilder elevation(double value) { elevation = value; return this; }
    public KubeLandmarkSceneBuilder duration(double minimum, double maximum) { minimumDuration = minimum; maximumDuration = maximum; return this; }
    public KubeLandmarkSceneBuilder fov(double value) { fov = OptionalDouble.of(value); return this; }

    public KubeLandmarkSceneBuilder transition(String value) {
        transition = switch (value.toLowerCase(Locale.ROOT)) {
            case "cut" -> TransitionSpec.cut();
            case "damped" -> TransitionSpec.damped(0.7);
            case "match_move", "matchmove" -> TransitionSpec.matchMove();
            case "continue_orbit", "continueorbit" -> TransitionSpec.continueOrbit();
            default -> throw new IllegalArgumentException("Unknown transition '" + value
                    + "'; expected cut, damped, match_move, or continue_orbit");
        };
        return this;
    }

    CinematicPreset build() {
        return new LandmarkScenePreset(NamespacedId.parse(id), NamespacedId.parse(landmarkId), style, weight,
                distanceScale, speed, elevation, new CinematicPreset.DurationRange(minimumDuration, maximumDuration),
                transition, fov);
    }
}
