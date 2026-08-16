package dev.theonlytazz.idlecinematics.api;

import dev.theonlytazz.idlecinematics.client.shots.ShotRegistry;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/** Fired once on the client mod bus before the preset registry is frozen. */
public final class RegisterCinematicPresetsEvent extends Event implements IModBusEvent {
    private final ShotRegistry registry;

    public RegisterCinematicPresetsEvent(ShotRegistry registry) { this.registry = registry; }
    public void register(CinematicPreset preset) { registry.register(preset); }
}
