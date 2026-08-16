package dev.theonlytazz.idlecinematics.integration.kubejs;

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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class KubeLandmarkBuilderTest {
    @Test
    void buildsDeclarativeLandmarkWithAliases() {
        var definition = new KubeLandmarkBuilder("pack:altar", "example:altar")
                .block("example:upgraded_altar").offset(0.5, 1.25, 0.5).radius(4.0)
                .score(3.0).searchRadius(32.0).tag("magic").build();

        assertEquals(new NamespacedId("pack", "altar"), definition.id());
        assertEquals(2, definition.blocks().size());
        assertEquals(32.0, definition.searchRadius());
    }

    @Test
    void buildsFiniteParameterizedScene() {
        CinematicPreset preset = new KubeLandmarkSceneBuilder("pack:altar_orbit", "pack:altar", "orbit")
                .weight(1.5).distance(1.8).speed(12.0).elevation(22.0).duration(7.0, 11.0)
                .transition("continue_orbit").fov(58.0).build();
        CinematicLandmark landmark = new CinematicLandmark(new NamespacedId("pack", "altar"),
                new NamespacedId("example", "altar"), BlockPos.ZERO, new Vec3(0.5, 1.25, 0.5),
                4.0, 3.0, Set.of("magic"));
        CinematicContext base = TestContexts.context(false, CinematicContext.DimensionKind.OVERWORLD,
                CinematicContext.DayPhase.DAY, CinematicContext.Weather.CLEAR, true, 12, Optional.empty());
        CinematicContext context = new CinematicContext(base.player(), base.dimension(), base.dayPhase(), base.enclosed(),
                base.openSky(), base.weather(), base.fluidState(), base.lightLevel(), base.effectiveRenderDistance(),
                base.ceilingClearance(), base.floorDrop(), base.directions(), base.nearbySubjects(), base.selectedSubject(),
                base.terrainTarget(), base.celestialTarget(), List.of(landmark), Optional.of(landmark));

        assertEquals(4.5, preset.contextScore(context));
        var state = preset.createMotion(context, landmark.subject(), new java.util.Random(2)).sample(0.5, 3.0);
        assertTrue(Double.isFinite(state.resolvePosition(1.0).length()));
        assertEquals(58.0, state.cinematicFov().orElseThrow());
    }

    @Test
    void rejectsUnknownStylesTransitionsAndFov() {
        assertThrows(IllegalArgumentException.class,
                () -> new KubeLandmarkSceneBuilder("pack:scene", "pack:altar", "spiral"));
        assertThrows(IllegalArgumentException.class,
                () -> new KubeLandmarkSceneBuilder("pack:scene", "pack:altar", "orbit").transition("fade"));
        assertThrows(IllegalArgumentException.class,
                () -> new KubeLandmarkSceneBuilder("pack:scene", "pack:altar", "orbit").fov(200.0).build());
    }
}
