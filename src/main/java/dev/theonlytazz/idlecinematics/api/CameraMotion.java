package dev.theonlytazz.idlecinematics.api;

@FunctionalInterface
public interface CameraMotion {
    CinematicRigState sample(double progress, double elapsedSeconds);
}
