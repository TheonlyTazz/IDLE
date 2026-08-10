package dev.theonlytazz.idlecinematics.client;

import java.util.Set;
import java.util.random.RandomGenerator;

public interface ShotPreset {
    String id();

    Set<ShotTag> tags();

    double contextWeight(SceneContext scene);

    ShotPlan createPlan(SceneContext scene, RandomGenerator random, int configuredDurationTicks);
}
