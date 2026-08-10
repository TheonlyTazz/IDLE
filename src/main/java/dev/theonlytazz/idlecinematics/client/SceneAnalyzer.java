package dev.theonlytazz.idlecinematics.client;

import dev.theonlytazz.idlecinematics.platform.ClientWorldAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.Optional;

public final class SceneAnalyzer {
    private static final Vec3[] HORIZONTAL_DIRECTIONS = {
            new Vec3(1, 0, 0), new Vec3(-1, 0, 0), new Vec3(0, 0, 1), new Vec3(0, 0, -1)
    };

    public SceneContext analyze(Minecraft minecraft, boolean includeEntities) {
        if (minecraft.player == null || minecraft.level == null) throw new IllegalStateException("No client world");
        Vec3 eye = minecraft.player.getEyePosition();
        Vec3 playerFocus = minecraft.player.position().add(0.0, minecraft.player.getEyeHeight() * 0.62, 0.0);
        SceneContext.DimensionKind dimension = minecraft.level.dimension() == Level.NETHER ? SceneContext.DimensionKind.NETHER
                : minecraft.level.dimension() == Level.END ? SceneContext.DimensionKind.END
                : minecraft.level.dimension() == Level.OVERWORLD ? SceneContext.DimensionKind.OVERWORLD
                : SceneContext.DimensionKind.OTHER;
        boolean openSky = minecraft.level.canSeeSky(BlockPos.containing(eye));
        int openDirections = 0;
        for (Vec3 direction : HORIZONTAL_DIRECTIONS) {
            if (clearance(minecraft, eye, direction, 8.0) >= 7.9) openDirections++;
        }
        double ceiling = clearance(minecraft, eye, new Vec3(0, 1, 0), 12.0);
        boolean enclosed = dimension != SceneContext.DimensionKind.NETHER && !openSky
                && (openDirections <= 2 || ceiling < 4.5);
        long time = Math.floorMod(ClientWorldAdapter.dayTime(minecraft.level), 24000L);
        SceneContext.DayPhase phase = time < 2000L ? SceneContext.DayPhase.SUNRISE
                : time < 11000L ? SceneContext.DayPhase.DAY
                : time < 14000L ? SceneContext.DayPhase.SUNSET : SceneContext.DayPhase.NIGHT;
        Optional<Vec3> entityFocus = includeEntities ? minecraft.level.getEntitiesOfClass(LivingEntity.class,
                        minecraft.player.getBoundingBox().inflate(12.0), entity -> entity != minecraft.player && entity.isAlive()).stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(minecraft.player)))
                .map(entity -> playerFocus.lerp(entity.getBoundingBox().getCenter(), 0.42)) : Optional.empty();
        return new SceneContext(playerFocus, entityFocus, dimension, phase, openSky, enclosed, openDirections, ceiling);
    }

    private double clearance(Minecraft minecraft, Vec3 origin, Vec3 direction, double distance) {
        Vec3 end = origin.add(direction.scale(distance));
        HitResult hit = minecraft.level.clip(new ClipContext(origin, end, ClipContext.Block.VISUAL,
                ClipContext.Fluid.NONE, minecraft.player));
        return hit.getType() == HitResult.Type.MISS ? distance : origin.distanceTo(hit.getLocation());
    }
}
