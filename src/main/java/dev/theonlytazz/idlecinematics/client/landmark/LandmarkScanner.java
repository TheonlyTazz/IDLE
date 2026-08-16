package dev.theonlytazz.idlecinematics.client.landmark;

import dev.theonlytazz.idlecinematics.api.CinematicLandmark;
import dev.theonlytazz.idlecinematics.api.LandmarkCandidate;
import dev.theonlytazz.idlecinematics.core.NamespacedId;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Scans block entities from already-loaded chunks; it never requests or generates a chunk. */
public final class LandmarkScanner {
    static final double MAX_RADIUS = 48.0;
    private static final int MAX_RESULTS = 64;
    private static final int MAX_MATCHING_BLOCK_ENTITIES = 256;
    private final LandmarkRegistry registry;

    public LandmarkScanner() { this(LandmarkRegistry.active()); }
    LandmarkScanner(LandmarkRegistry registry) { this.registry = registry; }

    public List<CinematicLandmark> scan(ClientLevel level, Vec3 origin) {
        int centerX = BlockPos.containing(origin).getX() >> 4;
        int centerZ = BlockPos.containing(origin).getZ() >> 4;
        int chunkRadius = (int) Math.ceil(MAX_RADIUS / 16.0);
        List<ScoredLandmark> found = new ArrayList<>();
        int matchingCandidates = 0;
        scan:
        for (int chunkX = centerX - chunkRadius; chunkX <= centerX + chunkRadius; chunkX++) {
            for (int chunkZ = centerZ - chunkRadius; chunkZ <= centerZ + chunkRadius; chunkZ++) {
                if (!level.getChunkSource().hasChunk(chunkX, chunkZ)) continue;
                ChunkAccess chunk = level.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
                if (!(chunk instanceof LevelChunk levelChunk)) continue;
                for (var entry : levelChunk.getBlockEntities().entrySet()) {
                    BlockPos position = entry.getKey();
                    var blockEntity = entry.getValue();
                    double distance = origin.distanceTo(Vec3.atCenterOf(position));
                    if (distance > MAX_RADIUS) continue;
                    NamespacedId blockId = NamespacedId.parse(BuiltInRegistries.BLOCK.getKey(blockEntity.getBlockState().getBlock()).toString());
                    if (!registry.recognizes(blockId)) continue;
                    if (matchingCandidates++ >= MAX_MATCHING_BLOCK_ENTITIES) break scan;
                    LandmarkCandidate candidate = new LandmarkCandidate(level, position, blockEntity.getBlockState(),
                            Optional.of(blockEntity), blockId);
                    for (CinematicLandmark landmark : registry.detect(candidate, distance)) {
                        double distanceFactor = Math.max(0.2, 1.0 - distance / (MAX_RADIUS * 1.25));
                        found.add(new ScoredLandmark(landmark, landmark.score() * distanceFactor));
                    }
                }
            }
        }
        return found.stream().sorted(Comparator.comparingDouble(ScoredLandmark::effectiveScore).reversed())
                .limit(MAX_RESULTS).map(ScoredLandmark::landmark).toList();
    }

    private record ScoredLandmark(CinematicLandmark landmark, double effectiveScore) {}
}
