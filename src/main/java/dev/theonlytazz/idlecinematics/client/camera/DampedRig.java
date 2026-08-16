package dev.theonlytazz.idlecinematics.client.camera;

import dev.theonlytazz.idlecinematics.api.CinematicRigState;
import dev.theonlytazz.idlecinematics.api.TransitionSpec;
import dev.theonlytazz.idlecinematics.core.CriticallyDampedValue;
import net.minecraft.world.phys.Vec3;
import java.util.OptionalDouble;

/** Owns all physical damping velocities; call {@link #clear()} on every immediate exit. */
public final class DampedRig {
    private final CriticallyDampedValue[] values = new CriticallyDampedValue[13];
    private CinematicRigState current;
    private boolean initialized;

    public CinematicRigState update(CinematicRigState target, TransitionSpec transition, double elapsedSeconds) {
        if (!initialized || transition.type() == TransitionSpec.Type.CUT) {
            snap(target);
            return current;
        }
        double position = transition.positionResponseSeconds();
        double rotation = transition.rotationResponseSeconds();
        double focus = transition.focusResponseSeconds();
        Vec3 anchor = new Vec3(update(0, target.anchor().x, elapsedSeconds, position),
                update(1, target.anchor().y, elapsedSeconds, position), update(2, target.anchor().z, elapsedSeconds, position));
        Vec3 focusPoint = new Vec3(update(3, target.focus().x, elapsedSeconds, focus),
                update(4, target.focus().y, elapsedSeconds, focus), update(5, target.focus().z, elapsedSeconds, focus));
        double distance = update(6, target.distance(), elapsedSeconds, position);
        double azimuth = values[7].updateAngle(target.azimuth(), elapsedSeconds, rotation,
                target.yawMode() == CinematicRigState.YawMode.FORWARD_ONLY);
        double elevation = update(8, target.elevation(), elapsedSeconds, rotation);
        double lateral = update(9, target.lateralOffset(), elapsedSeconds, position);
        double vertical = update(10, target.verticalOffset(), elapsedSeconds, position);
        double roll = values[11].updateAngle(target.roll(), elapsedSeconds, rotation, false);
        OptionalDouble fov = target.cinematicFov().isPresent()
                ? OptionalDouble.of(update(12, target.cinematicFov().getAsDouble(), elapsedSeconds, rotation)) : OptionalDouble.empty();
        current = new CinematicRigState(anchor, focusPoint, distance, azimuth, elevation, lateral, vertical, roll,
                fov, target.subject(), target.yawMode());
        return current;
    }

    public void snap(CinematicRigState state) {
        double[] initial = {state.anchor().x, state.anchor().y, state.anchor().z, state.focus().x, state.focus().y,
                state.focus().z, state.distance(), state.azimuth(), state.elevation(), state.lateralOffset(),
                state.verticalOffset(), state.roll(), state.cinematicFov().orElse(70.0)};
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null) values[i] = new CriticallyDampedValue(initial[i]); else values[i].snap(initial[i]);
        }
        current = state;
        initialized = true;
    }

    public void clear() { initialized = false; current = null; for (CriticallyDampedValue value : values) if (value != null) value.snap(0.0); }
    public CinematicRigState current() { return current; }
    private double update(int index, double target, double elapsed, double response) { return values[index].update(target, elapsed, response); }
}
