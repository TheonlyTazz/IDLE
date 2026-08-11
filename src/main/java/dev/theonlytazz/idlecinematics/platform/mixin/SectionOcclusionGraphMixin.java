package dev.theonlytazz.idlecinematics.platform.mixin;

import dev.theonlytazz.idlecinematics.client.render.CinematicTerrainVisibility;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SectionOcclusionGraph.class)
abstract class SectionOcclusionGraphMixin {
    @Inject(method = {
            "addSectionsInFrustum(Lnet/minecraft/client/renderer/culling/Frustum;Ljava/util/List;)V",
            "addSectionsInFrustum(Lnet/minecraft/client/renderer/culling/Frustum;Ljava/util/List;Ljava/util/List;)V"
    }, at = @At("HEAD"), require = 0)
    private void idlecinematics$beginBroadTerrainPass(CallbackInfo callback) {
        CinematicTerrainVisibility.beginSectionGraphPass();
    }

    @Inject(method = {
            "addSectionsInFrustum(Lnet/minecraft/client/renderer/culling/Frustum;Ljava/util/List;)V",
            "addSectionsInFrustum(Lnet/minecraft/client/renderer/culling/Frustum;Ljava/util/List;Ljava/util/List;)V"
    }, at = @At("RETURN"), require = 0)
    private void idlecinematics$endBroadTerrainPass(CallbackInfo callback) {
        CinematicTerrainVisibility.endSectionGraphPass();
    }
}
