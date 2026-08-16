package dev.theonlytazz.idlecinematics.core;

/** Exact critically damped spring integration for a stationary target. */
public final class CriticallyDampedValue {
    private static final double MAX_DELTA_SECONDS = 0.1;
    private static final double EPSILON = 1.0e-5;
    private double value;
    private double velocity;

    public CriticallyDampedValue(double initial) { snap(initial); }

    public double update(double target, double elapsedSeconds, double responseSeconds) {
        if (!Double.isFinite(target)) target = value;
        double dt = Math.max(0.0, Math.min(MAX_DELTA_SECONDS, elapsedSeconds));
        if (responseSeconds <= EPSILON || dt == 0.0) {
            if (responseSeconds <= EPSILON) snap(target);
            return value;
        }
        double omega = 2.0 / responseSeconds;
        double change = value - target;
        double coefficient = velocity + omega * change;
        double decay = Math.exp(-omega * dt);
        value = target + (change + coefficient * dt) * decay;
        velocity = (velocity - omega * coefficient * dt) * decay;
        if (!Double.isFinite(value) || !Double.isFinite(velocity)) snap(target);
        if (Math.abs(value - target) < EPSILON && Math.abs(velocity) < EPSILON) snap(target);
        return value;
    }

    public double updateAngle(double target, double elapsedSeconds, double responseSeconds, boolean forwardOnly) {
        double adjusted = forwardOnly ? forwardTarget(value, target) : value + shortestDelta(value, target);
        return update(adjusted, elapsedSeconds, responseSeconds);
    }

    public void snap(double target) { value = Double.isFinite(target) ? target : 0.0; velocity = 0.0; }
    public double value() { return value; }
    public double velocity() { return velocity; }

    public static double shortestDelta(double from, double to) {
        double delta = (to - from + 180.0) % 360.0;
        if (delta < 0.0) delta += 360.0;
        delta -= 180.0;
        return delta == -180.0 ? 180.0 : delta;
    }

    private static double forwardTarget(double from, double target) {
        double delta = (target - from) % 360.0;
        if (delta < 0.0) delta += 360.0;
        return from + delta;
    }
}
