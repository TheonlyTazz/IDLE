package dev.theonlytazz.idlecinematics.client.camera;

import net.minecraft.world.phys.Vec3;

public record CameraPose(Vec3 position, float yaw, float pitch, float roll) {}
