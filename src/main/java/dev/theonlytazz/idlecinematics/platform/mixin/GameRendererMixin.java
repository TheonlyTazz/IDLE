package dev.theonlytazz.idlecinematics.platform.mixin;

import dev.theonlytazz.idlecinematics.client.ClientRuntime;
import net.minecraft.client.CameraType;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Keeps first-person hands out of cinematic frames without changing the player's perspective option. */
@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    @Redirect(method = "renderItemInHand", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"))
    private boolean idlecinematics$hideFirstPersonHands(CameraType cameraType) {
        return !ClientRuntime.isCinematicActive() && cameraType.isFirstPerson();
    }
}
