package dev.theonlytazz.idlecinematics.api;

public record SafetyPolicy(double minimumDistance, double maximumDistance, double minimumPitch, double maximumPitch,
                           double collisionRadius, FluidPolicy fluidPolicy, double obstructionTolerance) {
    public enum FluidPolicy { REJECT, ALLOW_WATER, ALLOW_ALL }

    public SafetyPolicy {
        minimumDistance = Math.max(0.25, minimumDistance);
        maximumDistance = Math.max(minimumDistance, maximumDistance);
        minimumPitch = Math.max(-89.0, Math.min(89.0, minimumPitch));
        maximumPitch = Math.max(minimumPitch, Math.min(89.0, maximumPitch));
        collisionRadius = Math.max(0.0, Math.min(1.0, collisionRadius));
        obstructionTolerance = Math.max(0.0, Math.min(1.0, obstructionTolerance));
    }

    public static SafetyPolicy standard() { return new SafetyPolicy(1.15, 16.0, -82.0, 82.0, 0.22, FluidPolicy.REJECT, 0.15); }
    public static SafetyPolicy cave() { return new SafetyPolicy(0.9, 5.0, -70.0, 70.0, 0.18, FluidPolicy.REJECT, 0.1); }
}
