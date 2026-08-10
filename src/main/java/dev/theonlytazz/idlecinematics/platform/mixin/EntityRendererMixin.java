package dev.theonlytazz.idlecinematics.platform.mixin;

import dev.theonlytazz.idlecinematics.client.ClientRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
abstract class EntityRendererMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void idlecinematics$keepLocalPlayerInRenderList(Entity entity, Frustum frustum,
                                                             double cameraX, double cameraY, double cameraZ,
                                                             CallbackInfoReturnable<Boolean> callback) {
        if (ClientRuntime.isCinematicActive() && entity == Minecraft.getInstance().player) {
            callback.setReturnValue(true);
        }
    }
}
