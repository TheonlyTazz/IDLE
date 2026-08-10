package dev.theonlytazz.idlecinematics.platform.mixin;

import dev.theonlytazz.idlecinematics.client.render.CinematicTerrainVisibility;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Frustum.class)
abstract class FrustumMixin {
    @Inject(method = "cubeInFrustum(Lnet/minecraft/world/level/levelgen/structure/BoundingBox;)I",
            at = @At("HEAD"), cancellable = true)
    private void idlecinematics$keepTerrainBranchesReady(BoundingBox box,
                                                          CallbackInfoReturnable<Integer> callback) {
        if (CinematicTerrainVisibility.shouldKeepTerrainReady()) callback.setReturnValue(-2);
    }

    @Inject(method = "isVisible", at = @At("HEAD"), cancellable = true)
    private void idlecinematics$keepTerrainLeavesReady(AABB box,
                                                        CallbackInfoReturnable<Boolean> callback) {
        if (CinematicTerrainVisibility.shouldKeepTerrainReady()) callback.setReturnValue(true);
    }
}
