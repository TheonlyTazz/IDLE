package dev.theonlytazz.idlecinematics.api;

import net.minecraft.world.phys.Vec3;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record CinematicSubject(Type type, Optional<UUID> entityId, Vec3 focus, double size, double movement) {
    public enum Type { PLAYER, ENTITY, TERRAIN, CELESTIAL, WORLD_POSITION }

    public CinematicSubject {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(entityId, "entityId");
        Objects.requireNonNull(focus, "focus");
        size = finiteNonNegative(size);
        movement = finiteNonNegative(movement);
        if ((type == Type.PLAYER || type == Type.ENTITY) && entityId.isEmpty()) {
            throw new IllegalArgumentException("Actor subjects require a UUID");
        }
    }

    public CinematicSubject withFocus(Vec3 newFocus) {
        return new CinematicSubject(type, entityId, newFocus, size, movement);
    }

    private static double finiteNonNegative(double value) {
        return Double.isFinite(value) ? Math.max(0.0, value) : 0.0;
    }
}
