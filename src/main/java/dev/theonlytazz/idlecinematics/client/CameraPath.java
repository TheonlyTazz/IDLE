package dev.theonlytazz.idlecinematics.client;

import net.minecraft.world.phys.Vec3;

@FunctionalInterface
public interface CameraPath {
    Vec3 offset(double progress, double phase);
}
