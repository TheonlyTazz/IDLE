package dev.theonlytazz.idlecinematics.api;

import dev.theonlytazz.idlecinematics.core.NamespacedId;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Declarative landmark matcher. Definitions are evaluated only for block entities in loaded client chunks.
 */
public final class CinematicLandmarkDefinition {
    private final NamespacedId id;
    private final Set<NamespacedId> blocks;
    private final Vec3 focusOffset;
    private final double radius;
    private final double score;
    private final double searchRadius;
    private final Set<String> tags;
    private final Predicate<LandmarkCandidate> filter;

    private CinematicLandmarkDefinition(Builder builder) {
        id = Objects.requireNonNull(builder.id, "id");
        blocks = Set.copyOf(builder.blocks);
        if (blocks.isEmpty()) throw new IllegalArgumentException("Landmark definitions require at least one block identifier");
        focusOffset = Objects.requireNonNull(builder.focusOffset, "focusOffset");
        radius = positive(builder.radius, "radius");
        score = positive(builder.score, "score");
        searchRadius = positive(builder.searchRadius, "searchRadius");
        tags = Set.copyOf(builder.tags);
        filter = Objects.requireNonNull(builder.filter, "filter");
    }

    public NamespacedId id() { return id; }
    public Set<NamespacedId> blocks() { return blocks; }
    public double searchRadius() { return searchRadius; }

    public Optional<CinematicLandmark> detect(LandmarkCandidate candidate) {
        Objects.requireNonNull(candidate, "candidate");
        if (!blocks.contains(candidate.blockId()) || !filter.test(candidate)) return Optional.empty();
        Vec3 focus = Vec3.atLowerCornerOf(candidate.position()).add(focusOffset);
        return Optional.of(new CinematicLandmark(id, candidate.blockId(), candidate.position(), focus, radius, score, tags));
    }

    public static Builder builder(String id) { return new Builder(NamespacedId.parse(id)); }
    public static Builder builder(NamespacedId id) { return new Builder(id); }

    private static double positive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) throw new IllegalArgumentException(name + " must be finite and positive");
        return value;
    }

    public static final class Builder {
        private final NamespacedId id;
        private final Set<NamespacedId> blocks = new LinkedHashSet<>();
        private Vec3 focusOffset = new Vec3(0.5, 0.5, 0.5);
        private double radius = 1.5;
        private double score = 1.0;
        private double searchRadius = 48.0;
        private final Set<String> tags = new LinkedHashSet<>();
        private Predicate<LandmarkCandidate> filter = candidate -> true;

        private Builder(NamespacedId id) { this.id = Objects.requireNonNull(id, "id"); }

        public Builder block(String id) { blocks.add(NamespacedId.parse(id)); return this; }
        public Builder blocks(String... ids) { for (String id : ids) block(id); return this; }
        public Builder focusOffset(double x, double y, double z) { focusOffset = new Vec3(x, y, z); return this; }
        public Builder radius(double value) { radius = value; return this; }
        public Builder score(double value) { score = value; return this; }
        public Builder searchRadius(double value) { searchRadius = value; return this; }
        public Builder tags(String... values) { tags.addAll(Set.of(values)); return this; }
        public Builder filter(Predicate<LandmarkCandidate> value) { filter = Objects.requireNonNull(value, "value"); return this; }
        public CinematicLandmarkDefinition build() { return new CinematicLandmarkDefinition(this); }
    }
}
