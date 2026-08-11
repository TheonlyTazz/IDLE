package dev.theonlytazz.idlecinematics.client.shots;

import dev.theonlytazz.idlecinematics.client.camera.CameraPath;

import net.minecraft.world.phys.Vec3;

public record ShotPlan(String presetId, ShotPool pool, Vec3 cameraAnchor, Vec3 focus, CameraPath path,
                       int durationTicks, int transitionTicks) {
    public Vec3 desiredPosition(double progress, double phase, double distanceScale) {
        Vec3 unscaled = cameraAnchor.add(path.offset(progress, phase));
        // Scale around the framing target, not the anchor. Landscape and celestial shots
        // intentionally place their focus away from the player; scaling around the anchor
        // would shift those shots off-center when the user changes camera distance.
        return focus.add(unscaled.subtract(focus).scale(distanceScale));
    }
}
