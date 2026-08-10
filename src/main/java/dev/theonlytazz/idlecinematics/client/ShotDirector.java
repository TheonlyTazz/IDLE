package dev.theonlytazz.idlecinematics.client;

import dev.theonlytazz.idlecinematics.config.ClientConfig;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class ShotDirector {
    private final ShotRegistry registry;
    private final Deque<String> recent = new ArrayDeque<>();
    private ShotTag lastCategory;
    private int categoryStreak;

    public ShotDirector(ShotRegistry registry) {
        this.registry = registry;
    }

    public ShotPlan next(SceneContext scene, ClientConfig.ShotMode mode, int configuredDurationTicks) {
        List<WeightedPreset> candidates = new ArrayList<>();
        ShotTag desiredCategory = desiredCategory(scene, mode);
        for (ShotPreset preset : registry.presets()) {
            if (!eligible(preset, scene, mode)) continue;
            if (desiredCategory != null && categoryOf(preset) != desiredCategory) continue;
            double weight = preset.contextWeight(scene) * historyFactor(preset.id());
            if (weight > 0.0) candidates.add(new WeightedPreset(preset, weight));
        }
        if (candidates.isEmpty() && desiredCategory != null) {
            for (ShotPreset preset : registry.presets()) {
                if (!eligible(preset, scene, mode)) continue;
                double weight = preset.contextWeight(scene) * historyFactor(preset.id());
                if (weight > 0.0) candidates.add(new WeightedPreset(preset, weight));
            }
        }
        ShotPreset selected = choose(candidates, scene);
        remember(selected);
        return selected.createPlan(scene, ThreadLocalRandom.current(), configuredDurationTicks);
    }

    public void reset() {
        recent.clear();
        lastCategory = null;
        categoryStreak = 0;
    }

    private boolean eligible(ShotPreset preset, SceneContext scene, ClientConfig.ShotMode mode) {
        boolean cavePreset = preset.tags().contains(ShotTag.CAVE);
        if (cavePreset != scene.enclosed()) return false;
        if (preset.tags().contains(ShotTag.OPEN_SKY) && !scene.openSky()) return false;
        if (preset.tags().contains(ShotTag.WIDE) && !scene.openArea()) return false;
        if (preset.tags().contains(ShotTag.ENTITY) && scene.nearbyEntityFocus().isEmpty()) return false;
        if (preset.tags().contains(ShotTag.NETHER) && scene.dimension() != SceneContext.DimensionKind.NETHER) return false;
        if (preset.tags().contains(ShotTag.END) && scene.dimension() != SceneContext.DimensionKind.END) return false;
        if (preset.tags().contains(ShotTag.NIGHT) && scene.dayPhase() != SceneContext.DayPhase.NIGHT) return false;
        if (scene.enclosed()) return true;
        return switch (mode) {
            case DYNAMIC -> true;
            case PLAYER_FOCUSED -> preset.tags().contains(ShotTag.PLAYER);
            case ENVIRONMENT_FOCUSED -> preset.tags().contains(ShotTag.ENVIRONMENT);
            case CLASSIC -> preset.id().equals("orbit") || preset.id().equals("wide_orbit");
        };
    }

    private double historyFactor(String id) {
        if (recent.peekFirst() != null && recent.peekFirst().equals(id)) return 0.0;
        int index = 0;
        for (String previous : recent) {
            if (previous.equals(id)) return index <= 1 ? 0.2 : 0.55;
            index++;
        }
        return 1.0;
    }

    private ShotTag desiredCategory(SceneContext scene, ClientConfig.ShotMode mode) {
        if (scene.enclosed()) return ShotTag.PLAYER;
        if (mode == ClientConfig.ShotMode.PLAYER_FOCUSED || mode == ClientConfig.ShotMode.CLASSIC) return ShotTag.PLAYER;
        if (mode == ClientConfig.ShotMode.ENVIRONMENT_FOCUSED) return ShotTag.ENVIRONMENT;
        if (lastCategory == ShotTag.PLAYER && categoryStreak >= 2) return ShotTag.ENVIRONMENT;
        if (lastCategory == ShotTag.ENVIRONMENT && categoryStreak >= 3) return ShotTag.PLAYER;
        return ThreadLocalRandom.current().nextDouble() < 0.45 ? ShotTag.PLAYER : ShotTag.ENVIRONMENT;
    }

    private ShotPreset choose(List<WeightedPreset> candidates, SceneContext scene) {
        if (candidates.isEmpty()) {
            String fallback = scene.enclosed() ? "cave_close" : "tight_orbit";
            return registry.presets().stream().filter(preset -> preset.id().equals(fallback)).findFirst()
                    .orElseThrow(() -> new IllegalStateException("Missing fallback preset: " + fallback));
        }
        double total = candidates.stream().mapToDouble(WeightedPreset::weight).sum();
        double cursor = ThreadLocalRandom.current().nextDouble(total);
        for (WeightedPreset candidate : candidates) {
            cursor -= candidate.weight;
            if (cursor <= 0.0) return candidate.preset;
        }
        return candidates.getLast().preset;
    }

    private void remember(ShotPreset preset) {
        recent.addFirst(preset.id());
        while (recent.size() > 4) recent.removeLast();
        ShotTag category = categoryOf(preset);
        if (category == lastCategory) categoryStreak++; else {
            lastCategory = category;
            categoryStreak = 1;
        }
    }

    private ShotTag categoryOf(ShotPreset preset) {
        return preset.tags().contains(ShotTag.ENVIRONMENT) ? ShotTag.ENVIRONMENT : ShotTag.PLAYER;
    }

    private record WeightedPreset(ShotPreset preset, double weight) {}
}
