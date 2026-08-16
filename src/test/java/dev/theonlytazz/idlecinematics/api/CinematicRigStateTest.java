package dev.theonlytazz.idlecinematics.api;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.OptionalDouble;
import static org.junit.jupiter.api.Assertions.*;

final class CinematicRigStateTest {
    @Test void cameraDistanceIsInvariantAroundAnchor() {
        CinematicSubject subject = new CinematicSubject(CinematicSubject.Type.WORLD_POSITION, Optional.empty(), new Vec3(20, 4, -2), 0, 0);
        CinematicRigState state = new CinematicRigState(new Vec3(10, 3, 8), subject.focus(), 6, 37, 18,
                0, 0, 0, OptionalDouble.empty(), subject, CinematicRigState.YawMode.SHORTEST_PATH);
        assertEquals(6, state.resolvePosition(1).distanceTo(state.anchor()), 1.0e-9);
        assertEquals(9, state.resolvePosition(1.5).distanceTo(state.anchor()), 1.0e-9);
    }

    @Test void zeroDistanceAndInvalidInputRemainFinite() {
        CinematicSubject subject = new CinematicSubject(CinematicSubject.Type.WORLD_POSITION, Optional.empty(), Vec3.ZERO, 0, 0);
        CinematicRigState state = new CinematicRigState(Vec3.ZERO, Vec3.ZERO, Double.NaN, Double.NaN, 0, 0, 0, 0,
                OptionalDouble.of(Double.NaN), subject, CinematicRigState.YawMode.SHORTEST_PATH);
        Vec3 result = state.resolvePosition(Double.POSITIVE_INFINITY);
        assertTrue(Double.isFinite(result.x) && Double.isFinite(result.y) && Double.isFinite(result.z));
        assertTrue(state.cinematicFov().isEmpty());
    }
}
