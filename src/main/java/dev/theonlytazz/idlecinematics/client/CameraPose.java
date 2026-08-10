package dev.theonlytazz.idlecinematics.client;

import net.minecraft.world.phys.Vec3;

public record CameraPose(Vec3 position, float yaw, float pitch, float roll) {}
