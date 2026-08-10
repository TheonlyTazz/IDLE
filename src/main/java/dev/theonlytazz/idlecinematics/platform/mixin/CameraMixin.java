package dev.theonlytazz.idlecinematics.platform.mixin;

import dev.theonlytazz.idlecinematics.client.ClientRuntime;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
abstract class CameraMixin {
    @Inject(method = "setup", at = @At("TAIL"))
    private void idlecinematics$applyPose(BlockGetter level, Entity entity, boolean detached,
                                           boolean reverse, float partialTick, CallbackInfo callback) {
        ClientRuntime.applyCamera((Camera) (Object) this, partialTick);
    }
}
