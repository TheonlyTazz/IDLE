package dev.theonlytazz.idlecinematics.client.shots;

import dev.theonlytazz.idlecinematics.api.CinematicContext;
import dev.theonlytazz.idlecinematics.api.CinematicPreset;
import dev.theonlytazz.idlecinematics.api.CinematicSubject;
import dev.theonlytazz.idlecinematics.config.ClientConfig;
import dev.theonlytazz.idlecinematics.core.NamespacedId;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;
import java.util.function.Predicate;

public final class ShotDirector {
    private final ShotRegistry registry;
    private final RandomGenerator random;
    private final Predicate<CinematicPreset> enabled;
    private final Deque<NamespacedId> recent = new ArrayDeque<>();
    private String lastCategory;
    private int categoryStreak;

    public ShotDirector(ShotRegistry registry, RandomGenerator random) {
        this(registry, random, preset -> true);
    }

    public ShotDirector(ShotRegistry registry, RandomGenerator random, Predicate<CinematicPreset> enabled) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.random = Objects.requireNonNull(random, "random");
        this.enabled = Objects.requireNonNull(enabled, "enabled");
    }

    public ShotPlan next(CinematicContext context, ClientConfig.ShotMode mode, int configuredDurationTicks) {
        String desiredPool = desiredPool(context, mode);
        List<WeightedPreset> candidates = candidates(context, mode, desiredPool);
        if (candidates.isEmpty()) candidates = candidates(context, mode, null);
        CinematicPreset selected = choose(candidates, context);
        remember(selected);
        CinematicSubject subject = selected.selectSubject(context, random);
        int sampled = (int) Math.round(selected.duration().sample(random) * 20.0);
        int duration = Math.max(40, Math.min(sampled, Math.max(40, configuredDurationTicks)));
        return new ShotPlan(selected, subject, selected.createMotion(context, subject, random), duration,
                selected.transition(), selected.safety());
    }

    public void reset() { recent.clear(); lastCategory = null; categoryStreak = 0; }

    private List<WeightedPreset> candidates(CinematicContext context, ClientConfig.ShotMode mode, String pool) {
        List<WeightedPreset> result = new ArrayList<>();
        for (CinematicPreset preset : registry.presets()) {
            if (pool != null && !preset.pool().equals(pool)) continue;
            if (!eligible(preset, context, mode)) continue;
            double weight = preset.contextScore(context) * historyFactor(preset.id());
            if (weight > 0.0 && Double.isFinite(weight)) result.add(new WeightedPreset(preset, weight));
        }
        return result;
    }

    private boolean eligible(CinematicPreset preset, CinematicContext context, ClientConfig.ShotMode mode) {
        if (!enabled.test(preset)) return false;
        boolean cave = preset.tags().contains("cave");
        if (!preset.tags().contains("landmark") && cave != context.enclosed()) return false;
        if (preset.tags().contains("open_sky") && !context.openSky()) return false;
        if (preset.tags().contains("wide") && (!context.openArea() || context.effectiveRenderDistance() < 8)) return false;
        if (preset.tags().contains("entity") && context.selectedSubject().filter(subject -> subject.type() == CinematicSubject.Type.ENTITY).isEmpty()) return false;
        if (preset.tags().contains("landmark") && context.selectedLandmark().isEmpty()) return false;
        if (preset.tags().contains("nether") && context.dimension() != CinematicContext.DimensionKind.NETHER) return false;
        if (preset.tags().contains("end") && context.dimension() != CinematicContext.DimensionKind.END) return false;
        if (mode == ClientConfig.ShotMode.CLASSIC) return preset.id().path().equals("orbit") || preset.id().path().equals("wide_orbit");
        return true;
    }

    private double historyFactor(NamespacedId id) {
        if (id.equals(recent.peekFirst())) return 0.0;
        int index = 0;
        for (NamespacedId previous : recent) {
            if (previous.equals(id)) return index <= 1 ? 0.2 : 0.55;
            index++;
        }
        return 1.0;
    }

    private String desiredPool(CinematicContext context, ClientConfig.ShotMode mode) {
        if (context.selectedLandmark().isPresent() && mode != ClientConfig.ShotMode.PLAYER_FOCUSED
                && mode != ClientConfig.ShotMode.CLASSIC && random.nextDouble() < 0.6) return "landmark";
        if (context.enclosed()) return "cave";
        if (mode == ClientConfig.ShotMode.PLAYER_FOCUSED || mode == ClientConfig.ShotMode.CLASSIC) return "player";
        if (mode == ClientConfig.ShotMode.ENVIRONMENT_FOCUSED) return environmentPool(context);
        boolean player = random.nextDouble() < 0.45;
        if ("player".equals(lastCategory) && categoryStreak >= 2) player = false;
        if ("environment".equals(lastCategory) && categoryStreak >= 3) player = true;
        return player ? "player" : environmentPool(context);
    }

    private String environmentPool(CinematicContext context) {
        if (context.selectedLandmark().isPresent() && random.nextDouble() < 0.65) return "landmark";
        if (context.dimension() == CinematicContext.DimensionKind.NETHER) return "nether";
        if (context.dimension() == CinematicContext.DimensionKind.END) return "end";
        if (context.selectedSubject().filter(subject -> subject.type() == CinematicSubject.Type.ENTITY).isPresent() && random.nextDouble() < 0.3) return "entity";
        if (context.openSky() && random.nextDouble() < 0.55) return context.dayPhase().name().toLowerCase(java.util.Locale.ROOT);
        return "landscape";
    }

    private CinematicPreset choose(List<WeightedPreset> candidates, CinematicContext context) {
        if (candidates.isEmpty()) {
            String fallback = context.enclosed() ? "cave_close_portrait" : "tight_orbit";
            return registry.require(new NamespacedId("idlecinematics", fallback));
        }
        double total = candidates.stream().mapToDouble(WeightedPreset::weight).sum();
        double cursor = random.nextDouble(total);
        for (WeightedPreset candidate : candidates) if ((cursor -= candidate.weight()) <= 0.0) return candidate.preset();
        return candidates.getLast().preset();
    }

    private void remember(CinematicPreset preset) {
        recent.addFirst(preset.id());
        while (recent.size() > 4) recent.removeLast();
        String category = preset.tags().contains("environment") ? "environment" : "player";
        if (category.equals(lastCategory)) categoryStreak++; else { lastCategory = category; categoryStreak = 1; }
    }

    private record WeightedPreset(CinematicPreset preset, double weight) {}
}
