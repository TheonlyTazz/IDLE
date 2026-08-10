package dev.theonlytazz.idlecinematics.core;

public final class SmoothMath {
    private SmoothMath() {}

    public static double smootherStep(double value) {
        double t = Math.max(0.0, Math.min(1.0, value));
        return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
    }

    public static float angle(float from, float to, double amount) {
        float delta = (to - from) % 360.0f;
        if (delta > 180.0f) delta -= 360.0f;
        if (delta < -180.0f) delta += 360.0f;
        return from + delta * (float) amount;
    }
}
