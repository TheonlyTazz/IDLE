package dev.theonlytazz.idlecinematics.client.shots;

import dev.theonlytazz.idlecinematics.api.CameraMotion;
import dev.theonlytazz.idlecinematics.api.CinematicPreset;
import dev.theonlytazz.idlecinematics.api.CinematicSubject;
import dev.theonlytazz.idlecinematics.api.SafetyPolicy;
import dev.theonlytazz.idlecinematics.api.TransitionSpec;

public record ShotPlan(CinematicPreset preset, CinematicSubject subject, CameraMotion motion,
                       int durationTicks, TransitionSpec transition, SafetyPolicy safety) {
    public String presetId() { return preset.id().toString(); }
    public String pool() { return preset.pool(); }
}
