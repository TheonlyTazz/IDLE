package dev.theonlytazz.idlecinematics.api;

import dev.theonlytazz.idlecinematics.client.shots.ShotRegistry;
import net.neoforged.neoforge.common.NeoForge;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Version-one entry point for add-on preset registration.
 *
 * <p>Add-ons may call {@link #register(CinematicPreset)} during their mod constructor, regardless of whether
 * IDLE's constructor has already run. Alternatively, they may subscribe to
 * {@link RegisterCinematicPresetsEvent}. Registration closes during client setup.</p>
 */
public final class CinematicPresets {
    public static final int API_VERSION = CinematicPreset.API_VERSION;
    private static boolean completed;

    private CinematicPresets() {}

    public static synchronized void register(CinematicPreset preset) {
        if (completed) throw new IllegalStateException("Cinematic preset registration is already complete");
        ShotRegistry.active().register(Objects.requireNonNull(preset, "preset"));
    }

    public static synchronized boolean isRegistrationOpen() { return !completed; }

    /** Initializes built-ins without closing registration. Safe to call more than once. */
    public static void bootstrap() { ShotRegistry.active(); }

    public static void completeRegistration() {
        completeRegistration(event -> NeoForge.EVENT_BUS.post(event));
    }

    static synchronized void completeRegistration(Consumer<RegisterCinematicPresetsEvent> dispatcher) {
        if (completed) return;
        ShotRegistry registry = ShotRegistry.active();
        dispatcher.accept(new RegisterCinematicPresetsEvent(registry));
        registry.freeze();
        completed = true;
    }
}
