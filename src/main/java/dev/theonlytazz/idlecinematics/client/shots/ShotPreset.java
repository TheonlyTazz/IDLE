package dev.theonlytazz.idlecinematics.client.shots;

import dev.theonlytazz.idlecinematics.client.scene.SceneContext;

import java.util.Set;
import java.util.random.RandomGenerator;

public interface ShotPreset {
    String id();

    ShotPool pool();

    Set<ShotTag> tags();

    double contextWeight(SceneContext scene);

    ShotPlan createPlan(SceneContext scene, RandomGenerator random, int configuredDurationTicks);
}
