package dev.theonlytazz.idlecinematics.platform.mixin;

import dev.theonlytazz.idlecinematics.client.ClientRuntime;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
abstract class CameraMixin {
    @Inject(method = "update", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;prepareCullFrustum(Lorg/joml/Matrix4fc;Lorg/joml/Matrix4f;Lnet/minecraft/world/phys/Vec3;)V",
            shift = At.Shift.BEFORE))
    private void idlecinematics$applyPose(DeltaTracker deltaTracker, CallbackInfo callback) {
        ClientRuntime.applyCamera((Camera) (Object) this, deltaTracker.getGameTimeDeltaPartialTick(true));
    }
}
