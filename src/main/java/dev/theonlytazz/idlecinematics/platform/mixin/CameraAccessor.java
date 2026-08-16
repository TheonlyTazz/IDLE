package dev.theonlytazz.idlecinematics.platform.mixin;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Camera.class)
public interface CameraAccessor {
    @Invoker("setPosition")
    void idlecinematics$setPosition(Vec3 position);

    @Invoker("setRotation")
    void idlecinematics$setRotation(float yaw, float pitch, float roll);

    @Accessor("detached")
    void idlecinematics$setDetached(boolean detached);
}
