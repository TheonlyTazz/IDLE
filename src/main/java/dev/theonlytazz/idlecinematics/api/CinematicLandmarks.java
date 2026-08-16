package dev.theonlytazz.idlecinematics.api;

import dev.theonlytazz.idlecinematics.client.landmark.LandmarkRegistry;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Objects;
import java.util.function.Consumer;

/** Version-one entry point for optional, block-entity-backed cinematic landmarks. */
public final class CinematicLandmarks {
    public static final int API_VERSION = 1;
    private static boolean completed;

    private CinematicLandmarks() {}

    public static synchronized void register(CinematicLandmarkDefinition definition) {
        if (completed) throw new IllegalStateException("Cinematic landmark registration is already complete");
        LandmarkRegistry.active().register(Objects.requireNonNull(definition, "definition"));
    }

    public static synchronized boolean isRegistrationOpen() { return !completed; }
    public static void bootstrap() { LandmarkRegistry.active(); }
    public static void completeRegistration() { completeRegistration(event -> NeoForge.EVENT_BUS.post(event)); }

    static synchronized void completeRegistration(Consumer<RegisterCinematicLandmarksEvent> dispatcher) {
        if (completed) return;
        LandmarkRegistry registry = LandmarkRegistry.active();
        dispatcher.accept(new RegisterCinematicLandmarksEvent(registry));
        registry.freeze();
        completed = true;
    }
}
