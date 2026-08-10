package dev.theonlytazz.idlecinematics.platform.mixin;

import dev.theonlytazz.idlecinematics.client.ClientRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
abstract class EntityRendererMixin {
    private static final double CINEMATIC_ENTITY_RENDER_RADIUS = 48.0D;

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void idlecinematics$keepLocalPlayerInRenderList(Entity entity, Frustum frustum,
                                                             double cameraX, double cameraY, double cameraZ,
                                                             CallbackInfoReturnable<Boolean> callback) {
        if (ClientRuntime.isCinematicActive()) {
            Minecraft minecraft = Minecraft.getInstance();
            if (entity == minecraft.player
                    || (entity instanceof LivingEntity living && living.isAlive()
                    && living.distanceToSqr(cameraX, cameraY, cameraZ)
                    <= CINEMATIC_ENTITY_RENDER_RADIUS * CINEMATIC_ENTITY_RENDER_RADIUS)) {
                // Cinematic paths can look back toward the player, outside the normal view frustum.
                // Keep nearby actors available to the entity render pass while retaining a hard range cap.
                callback.setReturnValue(true);
            }
        }
    }
}
