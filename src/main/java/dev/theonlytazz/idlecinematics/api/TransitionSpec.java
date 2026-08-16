package dev.theonlytazz.idlecinematics.api;

public record TransitionSpec(Type type, double durationSeconds, double positionResponseSeconds,
                             double rotationResponseSeconds, double focusResponseSeconds) {
    public enum Type { CUT, DAMPED, MATCH_MOVE, CONTINUE_ORBIT }

    public TransitionSpec {
        durationSeconds = clamp(durationSeconds, 0.0, 2.0);
        positionResponseSeconds = positive(positionResponseSeconds, 0.35);
        rotationResponseSeconds = positive(rotationResponseSeconds, 0.28);
        focusResponseSeconds = positive(focusResponseSeconds, 0.40);
    }

    public static TransitionSpec cut() { return new TransitionSpec(Type.CUT, 0.0, 0.35, 0.28, 0.40); }
    public static TransitionSpec damped(double duration) { return new TransitionSpec(Type.DAMPED, duration, 0.35, 0.28, 0.40); }
    public static TransitionSpec matchMove() { return new TransitionSpec(Type.MATCH_MOVE, 0.8, 0.35, 0.28, 0.40); }
    public static TransitionSpec continueOrbit() { return new TransitionSpec(Type.CONTINUE_ORBIT, 0.65, 0.35, 0.28, 0.40); }

    public TransitionSpec scaled(double intensity) {
        return new TransitionSpec(type, durationSeconds * clamp(intensity, 0.0, 2.0), positionResponseSeconds,
                rotationResponseSeconds, focusResponseSeconds);
    }

    private static double positive(double value, double fallback) { return Double.isFinite(value) && value > 0.0 ? value : fallback; }
    private static double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, Double.isFinite(value) ? value : min)); }
}
