package dev.theonlytazz.idlecinematics.client.camera;

import dev.theonlytazz.idlecinematics.api.*;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import java.util.OptionalDouble;
import static org.junit.jupiter.api.Assertions.*;

final class CameraEngineTest {
    private static CinematicRigState rig(double azimuth, Vec3 focus) {
        CinematicSubject subject = new CinematicSubject(CinematicSubject.Type.WORLD_POSITION, Optional.empty(), focus, 0, 0);
        return new CinematicRigState(Vec3.ZERO, focus, 5, azimuth, 10, 0, 0, 0,
                OptionalDouble.empty(), subject, CinematicRigState.YawMode.SHORTEST_PATH);
    }

    @Test void implementsAllTransitionPolicies() {
        CinematicRigState outgoing = rig(350, new Vec3(1, 2, 3));
        CinematicRigState incoming = rig(10, new Vec3(9, 8, 7));
        assertSame(incoming, RigTransitions.prepare(outgoing, incoming, TransitionSpec.cut()));
        assertSame(incoming, RigTransitions.prepare(outgoing, incoming, TransitionSpec.damped(1)));
        assertEquals(outgoing.focus(), RigTransitions.prepare(outgoing, incoming, TransitionSpec.matchMove()).focus());
        CinematicRigState continued = RigTransitions.prepare(outgoing, incoming, TransitionSpec.continueOrbit());
        assertTrue(continued.azimuth() >= outgoing.azimuth());
        assertEquals(CinematicRigState.YawMode.FORWARD_ONLY, continued.yawMode());
    }

    @Test void cutSnapsAndDampedConverges() {
        DampedRig damping = new DampedRig();
        damping.update(rig(0, Vec3.ZERO), TransitionSpec.cut(), 0.05);
        CinematicRigState snapped = damping.update(rig(90, new Vec3(5, 0, 0)), TransitionSpec.cut(), 0.05);
        assertEquals(90, snapped.azimuth());
        for (int i = 0; i < 200; i++) snapped = damping.update(rig(180, new Vec3(10, 0, 0)), TransitionSpec.damped(1), 0.05);
        assertEquals(180, snapped.azimuth(), 1.0e-3);
        assertEquals(10, snapped.focus().x, 1.0e-3);
        damping.clear();
        assertNull(damping.current());
    }

    @Test void fiveRayCollisionUsesNearestHitAndMargin() {
        CameraVolumeCollision.Result result = CameraVolumeCollision.resolve(Vec3.ZERO, new Vec3(10, 0, 0), 0.25, 0.2,
                (start, end) -> start.y > 0 ? OptionalDouble.of(0.5) : OptionalDouble.empty());
        assertTrue(result.collided());
        assertEquals(4.8, result.distance(), 1.0e-9);
        assertEquals(4.8, result.position().x, 1.0e-9);
    }
}
