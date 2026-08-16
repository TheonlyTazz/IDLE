package dev.theonlytazz.idlecinematics.api;

import dev.theonlytazz.idlecinematics.client.landmark.LandmarkRegistry;
import net.neoforged.bus.api.Event;

/** Fired once on NeoForge's client event bus immediately before the landmark registry is frozen. */
public final class RegisterCinematicLandmarksEvent extends Event {
    private final LandmarkRegistry registry;

    public RegisterCinematicLandmarksEvent(LandmarkRegistry registry) { this.registry = registry; }
    public void register(CinematicLandmarkDefinition definition) { registry.register(definition); }
}
