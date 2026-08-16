package dev.theonlytazz.idlecinematics.client.scene;

import dev.theonlytazz.idlecinematics.api.CinematicContext;
import dev.theonlytazz.idlecinematics.api.CinematicSubject;
import dev.theonlytazz.idlecinematics.client.shots.SubjectSelector;
import dev.theonlytazz.idlecinematics.api.CinematicLandmark;
import dev.theonlytazz.idlecinematics.client.landmark.LandmarkScanner;
import dev.theonlytazz.idlecinematics.platform.ClientWorldAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

/** Bounded, selection-time-only scene analysis. All probes remain inside loaded client chunks. */
public final class SceneAnalyzer {
    private static final int MAX_ENTITY_CANDIDATES = 64;
    private static final double ENTITY_RADIUS = 48.0;
    private static final Vec3[] DIRECTIONS = createDirections();
    private final SubjectSelector subjectSelector = new SubjectSelector();
    private final LandmarkScanner landmarkScanner = new LandmarkScanner();

    public CinematicContext analyze(Minecraft minecraft, boolean includeEntities, RandomGenerator random) {
        if (minecraft.player == null || minecraft.level == null) throw new IllegalStateException("No usable client world");
        Vec3 eye = minecraft.player.getEyePosition();
        Vec3 focus = minecraft.player.position().add(0.0, minecraft.player.getEyeHeight() * 0.62, 0.0);
        CinematicSubject player = new CinematicSubject(CinematicSubject.Type.PLAYER,
                Optional.of(minecraft.player.getUUID()), focus, minecraft.player.getBbHeight(), minecraft.player.getDeltaMovement().length());
        int renderDistance = minecraft.options.renderDistance().get();
        double probeDistance = Math.min(24.0, Math.max(12.0, renderDistance * 2.0));
        List<CinematicContext.DirectionalProbe> directions = new ArrayList<>(8);
        for (Vec3 direction : DIRECTIONS) {
            RayProbe eyeProbe = probe(minecraft, eye, direction, probeDistance);
            Vec3 groundOrigin = minecraft.player.position().add(0.0, 0.35, 0.0);
            RayProbe groundProbe = probe(minecraft, groundOrigin, direction, probeDistance);
            Vec3 floorOrigin = eye.add(direction.scale(Math.min(eyeProbe.distance() * 0.65, 8.0)));
            RayProbe floor = probe(minecraft, floorOrigin, new Vec3(0, -1, 0), 12.0);
            Optional<Vec3> foreground = groundProbe.hit().filter(hit -> groundProbe.distance() <= 4.5);
            directions.add(new CinematicContext.DirectionalProbe(direction, eyeProbe.distance(), foreground,
                    eyeProbe.hit(), floor.distance(), Math.min(eyeProbe.distance(), groundProbe.distance())));
        }
        RayProbe ceiling = probe(minecraft, eye, new Vec3(0, 1, 0), 16.0);
        RayProbe floor = probe(minecraft, eye, new Vec3(0, -1, 0), 16.0);
        boolean openSky = minecraft.level.canSeeSky(BlockPos.containing(eye));
        long dayTime = Math.floorMod(ClientWorldAdapter.dayTime(minecraft.level), 24000L);
        CinematicContext.DayPhase dayPhase = dayTime < 2000L ? CinematicContext.DayPhase.SUNRISE
                : dayTime < 11000L ? CinematicContext.DayPhase.DAY
                : dayTime < 14000L ? CinematicContext.DayPhase.SUNSET : CinematicContext.DayPhase.NIGHT;
        CinematicContext.DimensionKind dimension = minecraft.level.dimension() == Level.NETHER ? CinematicContext.DimensionKind.NETHER
                : minecraft.level.dimension() == Level.END ? CinematicContext.DimensionKind.END
                : minecraft.level.dimension() == Level.OVERWORLD ? CinematicContext.DimensionKind.OVERWORLD : CinematicContext.DimensionKind.OTHER;
        long openCount = directions.stream().filter(probe -> probe.openDistance() >= probeDistance - 0.1).count();
        boolean enclosed = dimension != CinematicContext.DimensionKind.NETHER && !openSky && (openCount <= 3 || ceiling.distance() < 4.5);

        List<CinematicSubject> subjects = includeEntities ? collectSubjects(minecraft) : List.of();
        List<SubjectSelector.Candidate> candidates = subjects.stream().map(subject -> new SubjectSelector.Candidate(subject, true,
                subject.focus().subtract(eye).normalize().dot(minecraft.player.getLookAngle()) * 0.2 + 0.2)).toList();
        CinematicSubject selected = subjectSelector.select(candidates, player, eye,
                candidate -> unobstructed(minecraft, eye, candidate.subject().focus()), random);
        Optional<CinematicSubject> selectedEntity = selected.type() == CinematicSubject.Type.ENTITY ? Optional.of(selected) : Optional.empty();

        CinematicContext.DirectionalProbe mostOpen = directions.stream().max(java.util.Comparator.comparingDouble(CinematicContext.DirectionalProbe::openDistance)).orElseThrow();
        double targetDistance = Math.min(14.0, Math.max(4.0, mostOpen.openDistance() * 0.65));
        CinematicSubject terrain = new CinematicSubject(CinematicSubject.Type.TERRAIN, Optional.empty(),
                focus.add(mostOpen.direction().scale(targetDistance)), 2.0, 0.0);
        double skyAngle = dayTime / 24000.0 * Math.PI * 2.0 - Math.PI / 2.0;
        CinematicSubject celestial = new CinematicSubject(CinematicSubject.Type.CELESTIAL, Optional.empty(),
                focus.add(Math.cos(skyAngle) * 24.0, Math.max(7.0, Math.sin(skyAngle) * 18.0 + 10.0), Math.sin(skyAngle) * 8.0), 4.0, 0.0);
        CinematicContext.Weather weather = minecraft.level.isThundering() ? CinematicContext.Weather.THUNDER
                : minecraft.level.isRaining() ? CinematicContext.Weather.RAIN : CinematicContext.Weather.CLEAR;
        CinematicContext.FluidState fluid = minecraft.player.isInLava() ? CinematicContext.FluidState.LAVA
                : minecraft.player.isInWater() ? CinematicContext.FluidState.WATER : CinematicContext.FluidState.DRY;
        int light = minecraft.level.getMaxLocalRawBrightness(BlockPos.containing(focus));
        List<CinematicLandmark> landmarks = landmarkScanner.scan(minecraft.level, focus);
        Optional<CinematicLandmark> selectedLandmark = landmarks.stream().findFirst();
        return new CinematicContext(player, dimension, dayPhase, enclosed, openSky, weather, fluid, light,
                renderDistance, ceiling.distance(), floor.distance(), directions, subjects, selectedEntity,
                Optional.of(terrain), openSky ? Optional.of(celestial) : Optional.empty(), landmarks, selectedLandmark);
    }

    public Optional<CinematicSubject> updateEntitySubject(Minecraft minecraft, CinematicSubject subject) {
        if (subject.type() != CinematicSubject.Type.ENTITY || subject.entityId().isEmpty() || minecraft.level == null) return Optional.of(subject);
        return minecraft.level.getEntitiesOfClass(LivingEntity.class,
                        minecraft.player == null ? new net.minecraft.world.phys.AABB(subject.focus(), subject.focus()).inflate(ENTITY_RADIUS)
                                : minecraft.player.getBoundingBox().inflate(ENTITY_RADIUS),
                        entity -> entity.getUUID().equals(subject.entityId().orElseThrow()) && valid(entity)).stream().findFirst()
                .map(entity -> subject.withFocus(entity.getBoundingBox().getCenter()));
    }

    public void reset() { subjectSelector.reset(); }

    private static List<CinematicSubject> collectSubjects(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null) return List.of();
        return minecraft.level.getEntitiesOfClass(LivingEntity.class, minecraft.player.getBoundingBox().inflate(ENTITY_RADIUS),
                        entity -> entity != minecraft.player && valid(entity)).stream().limit(MAX_ENTITY_CANDIDATES)
                .map(entity -> new CinematicSubject(CinematicSubject.Type.ENTITY, Optional.of(entity.getUUID()),
                        entity.getBoundingBox().getCenter(), Math.max(entity.getBbWidth(), entity.getBbHeight()),
                        entity.getDeltaMovement().length())).toList();
    }

    private static boolean valid(LivingEntity entity) {
        return entity.isAlive() && !entity.isRemoved() && !entity.isInvisible();
    }

    private static boolean unobstructed(Minecraft minecraft, Vec3 start, Vec3 end) {
        return minecraft.level != null && minecraft.level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, minecraft.player)).getType() == HitResult.Type.MISS;
    }

    private static RayProbe probe(Minecraft minecraft, Vec3 origin, Vec3 direction, double distance) {
        Vec3 end = origin.add(direction.scale(distance));
        if (minecraft.level == null || !minecraft.level.getChunkSource().hasChunk(
                net.minecraft.util.Mth.floor(end.x) >> 4, net.minecraft.util.Mth.floor(end.z) >> 4)) return new RayProbe(0.0, Optional.empty());
        HitResult hit = minecraft.level.clip(new ClipContext(origin, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, minecraft.player));
        return hit.getType() == HitResult.Type.MISS ? new RayProbe(distance, Optional.empty())
                : new RayProbe(origin.distanceTo(hit.getLocation()), Optional.of(hit.getLocation()));
    }

    private static Vec3[] createDirections() {
        Vec3[] values = new Vec3[8];
        for (int i = 0; i < values.length; i++) {
            double angle = i * Math.PI / 4.0;
            values[i] = new Vec3(Math.cos(angle), 0.0, Math.sin(angle));
        }
        return values;
    }

    private record RayProbe(double distance, Optional<Vec3> hit) {}
}
