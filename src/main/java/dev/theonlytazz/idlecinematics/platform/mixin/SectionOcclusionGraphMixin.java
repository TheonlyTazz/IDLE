package dev.theonlytazz.idlecinematics.platform.mixin;

import dev.theonlytazz.idlecinematics.client.render.CinematicTerrainVisibility;
import net.minecraft.client.renderer.SectionOcclusionGraph;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SectionOcclusionGraph.class)
abstract class SectionOcclusionGraphMixin {
    @Inject(method = "addSectionsInFrustum", at = @At("HEAD"))
    private void idlecinematics$beginBroadTerrainPass(Frustum frustum,
                                                       List<SectionRenderDispatcher.RenderSection> sections,
                                                       CallbackInfo callback) {
        CinematicTerrainVisibility.beginSectionGraphPass();
    }

    @Inject(method = "addSectionsInFrustum", at = @At("RETURN"))
    private void idlecinematics$endBroadTerrainPass(Frustum frustum,
                                                     List<SectionRenderDispatcher.RenderSection> sections,
                                                     CallbackInfo callback) {
        CinematicTerrainVisibility.endSectionGraphPass();
    }
}
