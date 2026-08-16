package dev.theonlytazz.idlecinematics.api;

import dev.theonlytazz.idlecinematics.client.shots.ShotRegistry;
import net.neoforged.bus.api.Event;

/** Fired once on NeoForge's client event bus immediately before the preset registry is frozen. */
public final class RegisterCinematicPresetsEvent extends Event {
    private final ShotRegistry registry;

    RegisterCinematicPresetsEvent(ShotRegistry registry) { this.registry = registry; }
    public void register(CinematicPreset preset) { registry.register(preset); }
}
