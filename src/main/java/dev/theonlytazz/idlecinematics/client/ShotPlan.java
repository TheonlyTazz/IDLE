package dev.theonlytazz.idlecinematics.client;

import net.minecraft.world.phys.Vec3;

public record ShotPlan(String presetId, ShotPool pool, Vec3 cameraAnchor, Vec3 focus, CameraPath path,
                       int durationTicks, int transitionTicks) {
    public Vec3 desiredPosition(double progress, double phase, double distanceScale) {
        return cameraAnchor.add(path.offset(progress, phase).scale(distanceScale));
    }
}
