package dev.theonlytazz.idlecinematics.config;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/** Version-neutral persistence helpers for individual cinematic preset choices. */
public final class PresetPreferences {
    private static final Set<String> LEGACY_NEW_PRESETS = Set.of(
            "idlecinematics:breathing_orbit",
            "idlecinematics:terrain_scout",
            "idlecinematics:foreground_parallax",
            "idlecinematics:rain_silhouette",
            "idlecinematics:entity_portrait");

    private PresetPreferences() {}

    public static Set<String> parseDisabled(String value) {
        if (value == null || value.isBlank()) return new LinkedHashSet<>();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(entry -> !entry.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static String encodeDisabled(Set<String> disabled) {
        return disabled.stream().sorted().collect(Collectors.joining(","));
    }

    public static boolean isEnabled(String id, Set<String> disabled, boolean legacyNewMotionsEnabled) {
        return !disabled.contains(id) && (legacyNewMotionsEnabled || !LEGACY_NEW_PRESETS.contains(id));
    }

    public static void migrateLegacyChoice(Set<String> disabled, boolean legacyNewMotionsEnabled) {
        if (!legacyNewMotionsEnabled) disabled.addAll(LEGACY_NEW_PRESETS);
    }
}
