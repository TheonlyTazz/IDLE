package dev.theonlytazz.idlecinematics.client.shots;

import dev.theonlytazz.idlecinematics.api.CinematicPreset;
import dev.theonlytazz.idlecinematics.core.NamespacedId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ShotRegistry {
    private static ShotRegistry active;
    private final Map<NamespacedId, CinematicPreset> presets = new LinkedHashMap<>();
    private boolean frozen;

    public synchronized ShotRegistry register(CinematicPreset preset) {
        if (frozen) throw new IllegalStateException("Cinematic preset registry is immutable");
        CinematicPreset previous = presets.putIfAbsent(preset.id(), preset);
        if (previous != null) throw new IllegalArgumentException("Duplicate cinematic preset identifier: " + preset.id());
        return this;
    }

    public synchronized List<CinematicPreset> presets() { return List.copyOf(presets.values()); }
    public synchronized CinematicPreset require(NamespacedId id) {
        CinematicPreset preset = presets.get(id);
        if (preset == null) throw new IllegalStateException("Missing cinematic preset: " + id);
        return preset;
    }
    public synchronized void freeze() { frozen = true; }
    public synchronized boolean frozen() { return frozen; }

    public static ShotRegistry createBuiltIns() {
        ShotRegistry registry = new ShotRegistry();
        BuiltInPresets.create().forEach(registry::register);
        return registry;
    }
    public static synchronized ShotRegistry active() {
        if (active == null) active = createBuiltIns();
        return active;
    }
}
