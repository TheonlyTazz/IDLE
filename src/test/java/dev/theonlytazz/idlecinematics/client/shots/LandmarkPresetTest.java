package dev.theonlytazz.idlecinematics.client.shots;

import dev.theonlytazz.idlecinematics.TestContexts;
import dev.theonlytazz.idlecinematics.api.CinematicContext;
import dev.theonlytazz.idlecinematics.api.CinematicLandmark;
import dev.theonlytazz.idlecinematics.api.CinematicPreset;
import dev.theonlytazz.idlecinematics.core.NamespacedId;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LandmarkPresetTest {
    @Test
    void landmarkPresetUsesSelectedLandmarkAsSubject() {
        CinematicLandmark landmark = new CinematicLandmark(new NamespacedId("addon", "altar"),
                new NamespacedId("example", "altar"), BlockPos.ZERO, new Vec3(4.5, 2.0, 7.5),
                4.0, 3.0, java.util.Set.of("magic"));
        CinematicContext base = TestContexts.context(false, CinematicContext.DimensionKind.OVERWORLD,
                CinematicContext.DayPhase.DAY, CinematicContext.Weather.CLEAR, true, 12, Optional.empty());
        CinematicContext context = new CinematicContext(base.player(), base.dimension(), base.dayPhase(), base.enclosed(),
                base.openSky(), base.weather(), base.fluidState(), base.lightLevel(), base.effectiveRenderDistance(),
                base.ceilingClearance(), base.floorDrop(), base.directions(), base.nearbySubjects(), base.selectedSubject(),
                base.terrainTarget(), base.celestialTarget(), List.of(landmark), Optional.of(landmark));
        CinematicPreset preset = BuiltInPresets.create().stream()
                .filter(value -> value.id().path().equals("landmark_orbit")).findFirst().orElseThrow();

        assertEquals(3.0, preset.contextScore(context));
        assertEquals(landmark.focus(), preset.selectSubject(context, new java.util.Random(1)).focus());
        var sample = preset.createMotion(context, landmark.subject(), new java.util.Random(1)).sample(0.5, 2.0);
        assertTrue(Double.isFinite(sample.distance()));
        assertEquals(landmark.focus(), sample.focus());
    }
}
