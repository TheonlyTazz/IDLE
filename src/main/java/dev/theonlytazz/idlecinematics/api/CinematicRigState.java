package dev.theonlytazz.idlecinematics.api;

import net.minecraft.world.phys.Vec3;
import java.util.Objects;
import java.util.OptionalDouble;

/** A semantic composition. Distance is applied before resolving a world-space camera pose. */
public record CinematicRigState(Vec3 anchor, Vec3 focus, double distance, double azimuth, double elevation,
                                double lateralOffset, double verticalOffset, double roll,
                                OptionalDouble cinematicFov, CinematicSubject subject, YawMode yawMode) {
    public enum YawMode { SHORTEST_PATH, FORWARD_ONLY }

    public CinematicRigState {
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(focus, "focus");
        Objects.requireNonNull(cinematicFov, "cinematicFov");
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(yawMode, "yawMode");
        distance = finite(distance, 0.0);
        azimuth = finite(azimuth, 0.0);
        elevation = finite(elevation, 0.0);
        lateralOffset = finite(lateralOffset, 0.0);
        verticalOffset = finite(verticalOffset, 0.0);
        roll = finite(roll, 0.0);
        if (cinematicFov.isPresent() && !Double.isFinite(cinematicFov.getAsDouble())) cinematicFov = OptionalDouble.empty();
    }

    public Vec3 resolvePosition(double distanceScale) {
        double safeDistance = Math.max(0.0, distance * Math.max(0.0, finite(distanceScale, 1.0)));
        double yaw = Math.toRadians(azimuth);
        double pitch = Math.toRadians(Math.max(-89.0, Math.min(89.0, elevation)));
        double horizontal = Math.cos(pitch) * safeDistance;
        Vec3 radial = new Vec3(Math.cos(yaw) * horizontal, Math.sin(pitch) * safeDistance, Math.sin(yaw) * horizontal);
        Vec3 tangent = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw)).scale(lateralOffset);
        return anchor.add(radial).add(tangent).add(0.0, verticalOffset, 0.0);
    }

    private static double finite(double value, double fallback) { return Double.isFinite(value) ? value : fallback; }
}
