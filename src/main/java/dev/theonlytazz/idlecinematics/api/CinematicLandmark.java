package dev.theonlytazz.idlecinematics.api;

import dev.theonlytazz.idlecinematics.core.NamespacedId;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** A stable, selection-time point of interest supplied by a landmark definition. */
public record CinematicLandmark(NamespacedId typeId, NamespacedId blockId, BlockPos origin, Vec3 focus,
                                double radius, double score, Set<String> tags) {
    public CinematicLandmark {
        Objects.requireNonNull(typeId, "typeId");
        Objects.requireNonNull(blockId, "blockId");
        Objects.requireNonNull(origin, "origin");
        Objects.requireNonNull(focus, "focus");
        tags = Set.copyOf(tags);
        radius = finitePositive(radius, 1.0);
        score = finitePositive(score, 1.0);
    }

    public CinematicSubject subject() {
        return new CinematicSubject(CinematicSubject.Type.WORLD_POSITION, Optional.empty(), focus, radius * 2.0, 0.0);
    }

    private static double finitePositive(double value, double fallback) {
        return Double.isFinite(value) && value > 0.0 ? value : fallback;
    }
}
