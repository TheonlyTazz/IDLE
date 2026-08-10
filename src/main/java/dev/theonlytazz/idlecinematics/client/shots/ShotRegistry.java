package dev.theonlytazz.idlecinematics.client.shots;

import java.util.ArrayList;
import java.util.List;

public final class ShotRegistry {
    private final List<ShotPreset> presets = new ArrayList<>();

    public ShotRegistry register(ShotPreset preset) {
        if (presets.stream().anyMatch(existing -> existing.id().equals(preset.id()))) {
            throw new IllegalArgumentException("Duplicate cinematic preset: " + preset.id());
        }
        presets.add(preset);
        return this;
    }

    public List<ShotPreset> presets() {
        return List.copyOf(presets);
    }

    public static ShotRegistry builtIns() {
        ShotRegistry registry = new ShotRegistry();
        BuiltInPresets.create().forEach(registry::register);
        return registry;
    }
}
