package dev.theonlytazz.idlecinematics.client.shots;

import dev.theonlytazz.idlecinematics.client.scene.SceneContext;

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
        ShotPool desiredPool = desiredPool(scene, mode);
        for (ShotPreset preset : registry.presets()) {
            if (!eligible(preset, scene, mode)) continue;
            if (preset.pool() != desiredPool) continue;
            double weight = preset.contextWeight(scene) * historyFactor(preset.id());
            if (weight > 0.0) candidates.add(new WeightedPreset(preset, weight));
        }
        if (candidates.isEmpty()) {
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
        if (mode == ClientConfig.ShotMode.CLASSIC) return preset.id().equals("orbit") || preset.id().equals("wide_orbit");
        return true;
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

    private ShotPool desiredPool(SceneContext scene, ClientConfig.ShotMode mode) {
        if (scene.enclosed()) return ShotPool.CAVE;
        if (mode == ClientConfig.ShotMode.PLAYER_FOCUSED || mode == ClientConfig.ShotMode.CLASSIC) return ShotPool.PLAYER;
        if (mode == ClientConfig.ShotMode.ENVIRONMENT_FOCUSED) return environmentPool(scene);
        boolean choosePlayer = ThreadLocalRandom.current().nextDouble() < 0.45;
        if (lastCategory == ShotTag.PLAYER && categoryStreak >= 2) choosePlayer = false;
        if (lastCategory == ShotTag.ENVIRONMENT && categoryStreak >= 3) choosePlayer = true;
        return choosePlayer ? ShotPool.PLAYER : environmentPool(scene);
    }

    private ShotPool environmentPool(SceneContext scene) {
        if (scene.dimension() == SceneContext.DimensionKind.NETHER) return ShotPool.NETHER;
        if (scene.dimension() == SceneContext.DimensionKind.END) return ShotPool.END;
        if (scene.nearbyEntityFocus().isPresent() && ThreadLocalRandom.current().nextDouble() < 0.3) return ShotPool.ENTITY;
        if (scene.openSky() && ThreadLocalRandom.current().nextDouble() < 0.6) {
            return switch (scene.dayPhase()) {
                case SUNRISE -> ShotPool.SUNRISE;
                case DAY -> ShotPool.DAY;
                case SUNSET -> ShotPool.SUNSET;
                case NIGHT -> ShotPool.NIGHT;
            };
        }
        return scene.nearbyEntityFocus().isPresent() && !scene.openArea() ? ShotPool.ENTITY : ShotPool.LANDSCAPE;
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
