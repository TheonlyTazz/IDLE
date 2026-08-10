package dev.theonlytazz.idlecinematics.platform.mixin;

import dev.theonlytazz.idlecinematics.client.ClientRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {
    @Inject(method = "isSectionCompiledAndVisible", at = @At("HEAD"), cancellable = true)
    private void idlecinematics$keepPlayerSectionVisible(BlockPos position,
                                                          CallbackInfoReturnable<Boolean> callback) {
        Minecraft minecraft = Minecraft.getInstance();
        if (ClientRuntime.isCinematicActive() && minecraft.player != null
                && position.equals(minecraft.player.blockPosition())) {
            callback.setReturnValue(true);
        }
    }
}
