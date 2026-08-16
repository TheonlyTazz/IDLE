package dev.theonlytazz.idlecinematics.integration.kubejs;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.theonlytazz.idlecinematics.api.CinematicLandmarks;
import dev.theonlytazz.idlecinematics.api.CinematicLandmarkDefinition;
import dev.theonlytazz.idlecinematics.api.CinematicPreset;
import dev.theonlytazz.idlecinematics.api.CinematicPresets;

import java.util.ArrayList;
import java.util.List;

/** Startup-script event that collects and validates declarations before committing them to IDLE's registries. */
public final class IdleKubeRegistrationEvent implements KubeEvent {
    private final List<KubeLandmarkBuilder> landmarks = new ArrayList<>();
    private final List<KubeLandmarkSceneBuilder> scenes = new ArrayList<>();

    public KubeLandmarkBuilder landmark(String id, String block) {
        KubeLandmarkBuilder builder = new KubeLandmarkBuilder(id, block);
        landmarks.add(builder);
        return builder;
    }

    public KubeLandmarkSceneBuilder scene(String id, String landmarkId, String style) {
        KubeLandmarkSceneBuilder builder = new KubeLandmarkSceneBuilder(id, landmarkId, style);
        scenes.add(builder);
        return builder;
    }

    void commit() {
        List<CinematicLandmarkDefinition> builtLandmarks = landmarks.stream().map(KubeLandmarkBuilder::build).toList();
        List<CinematicPreset> builtScenes = scenes.stream().map(KubeLandmarkSceneBuilder::build).toList();
        for (CinematicLandmarkDefinition landmark : builtLandmarks) CinematicLandmarks.register(landmark);
        for (CinematicPreset scene : builtScenes) CinematicPresets.register(scene);
    }
}
