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
        Vec3 openDirection = HORIZONTAL_DIRECTIONS[0];
        double greatestClearance = -1.0;
        Optional<Vec3> wallTarget = Optional.empty();
        double nearestWall = Double.MAX_VALUE;
        for (Vec3 direction : HORIZONTAL_DIRECTIONS) {
            RayProbe probe = probe(minecraft, eye, direction, 12.0);
            if (probe.distance() >= 11.9) openDirections++;
            if (probe.distance() > greatestClearance) {
                greatestClearance = probe.distance();
                openDirection = direction;
            }
            if (probe.hit().isPresent() && probe.distance() < nearestWall) {
                nearestWall = probe.distance();
                wallTarget = probe.hit();
            }
        }
        RayProbe ceilingProbe = probe(minecraft, eye, new Vec3(0, 1, 0), 12.0);
        RayProbe floorProbe = probe(minecraft, eye, new Vec3(0, -1, 0), 12.0);
        double ceiling = ceilingProbe.distance();
        boolean enclosed = dimension != SceneContext.DimensionKind.NETHER && !openSky
                && (openDirections <= 2 || ceiling < 4.5);
        long time = Math.floorMod(ClientWorldAdapter.dayTime(minecraft.level), 24000L);
        SceneContext.DayPhase phase = time < 2000L ? SceneContext.DayPhase.SUNRISE
                : time < 11000L ? SceneContext.DayPhase.DAY
                : time < 14000L ? SceneContext.DayPhase.SUNSET : SceneContext.DayPhase.NIGHT;
        double skyAngle = (time / 24000.0) * Math.PI * 2.0 - Math.PI / 2.0;
        Vec3 celestialTarget = playerFocus.add(Math.cos(skyAngle) * 24.0,
                Math.max(7.0, Math.sin(skyAngle) * 18.0 + 10.0), Math.sin(skyAngle) * 8.0);
        Optional<Vec3> entityFocus = includeEntities ? minecraft.level.getEntitiesOfClass(LivingEntity.class,
                        minecraft.player.getBoundingBox().inflate(12.0), entity -> entity != minecraft.player && entity.isAlive()).stream()
                .min(Comparator.comparingDouble(entity -> entity.distanceToSqr(minecraft.player)))
                .map(entity -> playerFocus.lerp(entity.getBoundingBox().getCenter(), 0.42)) : Optional.empty();
        return new SceneContext(playerFocus, entityFocus, openDirection, wallTarget, ceilingProbe.hit(), floorProbe.hit(),
                celestialTarget, dimension, phase, openSky, enclosed, openDirections, ceiling);
    }

    private RayProbe probe(Minecraft minecraft, Vec3 origin, Vec3 direction, double distance) {
        Vec3 end = origin.add(direction.scale(distance));
        HitResult hit = minecraft.level.clip(new ClipContext(origin, end, ClipContext.Block.VISUAL,
                ClipContext.Fluid.NONE, minecraft.player));
        return hit.getType() == HitResult.Type.MISS ? new RayProbe(distance, Optional.empty())
                : new RayProbe(origin.distanceTo(hit.getLocation()), Optional.of(hit.getLocation()));
    }

    private record RayProbe(double distance, Optional<Vec3> hit) {}
}
