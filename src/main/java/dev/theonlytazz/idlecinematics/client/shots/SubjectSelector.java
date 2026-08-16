package dev.theonlytazz.idlecinematics.client.shots;

import dev.theonlytazz.idlecinematics.api.CinematicSubject;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

public final class SubjectSelector {
    private final Deque<UUID> recent = new ArrayDeque<>();
    private boolean chooseFar;

    public CinematicSubject select(List<Candidate> candidates, CinematicSubject fallback, Vec3 viewer,
                                   Predicate<Candidate> visible, RandomGenerator random) {
        List<Scored> scored = new ArrayList<>();
        candidates.stream().limit(64).filter(Candidate::valid).filter(visible).forEach(candidate -> {
            double distance = Math.sqrt(candidate.subject().focus().distanceToSqr(viewer));
            double usefulness = 1.0 / (1.0 + Math.abs(distance - 16.0) / 16.0);
            double history = candidate.subject().entityId().map(id -> recent.contains(id) ? 0.3 : 1.0).orElse(1.0);
            double score = history * (usefulness + Math.min(2.0, candidate.subject().size()) * 0.35
                    + Math.min(1.0, candidate.subject().movement()) * 0.45 + candidate.screenUsefulness());
            if (score > 0.0 && Double.isFinite(score)) scored.add(new Scored(candidate.subject(), score, distance));
        });
        if (scored.isEmpty()) return fallback;
        scored.sort(Comparator.comparingDouble(Scored::distance));
        double median = scored.get(scored.size() / 2).distance();
        List<Scored> group = scored.stream().filter(value -> chooseFar ? value.distance() >= median : value.distance() <= median).toList();
        if (group.isEmpty()) group = scored;
        chooseFar = !chooseFar;
        CinematicSubject selected = weighted(group, random);
        selected.entityId().ifPresent(id -> { recent.addFirst(id); while (recent.size() > 4) recent.removeLast(); });
        return selected;
    }

    public void reset() { recent.clear(); chooseFar = false; }

    private static CinematicSubject weighted(List<Scored> values, RandomGenerator random) {
        double total = values.stream().mapToDouble(Scored::score).sum();
        double cursor = random.nextDouble(total);
        for (Scored value : values) { if ((cursor -= value.score()) <= 0.0) return value.subject(); }
        return values.getLast().subject();
    }

    public record Candidate(CinematicSubject subject, boolean valid, double screenUsefulness) {}
    private record Scored(CinematicSubject subject, double score, double distance) {}
}
