package dev.theonlytazz.idlecinematics.client.camera;

import net.minecraft.world.phys.Vec3;
import java.util.Objects;
import java.util.OptionalDouble;

/** Five-ray camera-volume approximation, independent from Minecraft's ray implementation. */
public final class CameraVolumeCollision {
    private CameraVolumeCollision() {}

    @FunctionalInterface public interface Raycaster { OptionalDouble hitFraction(Vec3 start, Vec3 end); }

    public static Result resolve(Vec3 focus, Vec3 desired, double radius, double safetyMargin, Raycaster raycaster) {
        return resolve(focus, desired, radius, safetyMargin, 0.0, raycaster);
    }

    /**
     * Resolves collision after skipping a declared subject volume around the focus. This lets a camera look at
     * the center of a solid landmark without treating the landmark itself as an intervening obstruction.
     */
    public static Result resolve(Vec3 focus, Vec3 desired, double radius, double safetyMargin,
                                 double subjectClearance, Raycaster raycaster) {
        Objects.requireNonNull(focus, "focus");
        Objects.requireNonNull(desired, "desired");
        Objects.requireNonNull(raycaster, "raycaster");
        Vec3 travel = desired.subtract(focus);
        double length = travel.length();
        if (!Double.isFinite(length) || length < 1.0e-8) return new Result(focus, false, 0.0);
        Vec3 direction = travel.scale(1.0 / length);
        double clearance = Double.isFinite(subjectClearance)
                ? Math.max(0.0, Math.min(subjectClearance, Math.max(0.0, length - 1.0e-6))) : 0.0;
        Vec3 rayStart = focus.add(direction.scale(clearance));
        double rayLength = length - clearance;
        Vec3 side = Math.abs(direction.y) > 0.95 ? new Vec3(1, 0, 0) : direction.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 up = side.cross(direction).normalize();
        Vec3[] offsets = {Vec3.ZERO, side.scale(radius), side.scale(-radius), up.scale(radius), up.scale(-radius)};
        double nearest = 1.0;
        for (Vec3 offset : offsets) {
            OptionalDouble hit = raycaster.hitFraction(rayStart.add(offset), desired.add(offset));
            if (hit.isPresent() && Double.isFinite(hit.getAsDouble())) nearest = Math.min(nearest, Math.max(0.0, hit.getAsDouble()));
        }
        if (nearest >= 1.0) return new Result(desired, false, length);
        double safeLength = Math.max(0.0, clearance + rayLength * nearest - Math.max(0.0, safetyMargin));
        return new Result(focus.add(direction.scale(safeLength)), true, safeLength);
    }

    public record Result(Vec3 position, boolean collided, double distance) {}
}
