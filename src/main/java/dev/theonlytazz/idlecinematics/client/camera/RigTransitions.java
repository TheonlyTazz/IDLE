package dev.theonlytazz.idlecinematics.client.camera;

import dev.theonlytazz.idlecinematics.api.CinematicRigState;
import dev.theonlytazz.idlecinematics.api.TransitionSpec;
import net.minecraft.world.phys.Vec3;
import java.util.OptionalDouble;

public final class RigTransitions {
    private RigTransitions() {}

    public static CinematicRigState prepare(CinematicRigState outgoing, CinematicRigState incoming, TransitionSpec spec) {
        if (outgoing == null || spec.type() == TransitionSpec.Type.CUT || spec.type() == TransitionSpec.Type.DAMPED) return incoming;
        return switch (spec.type()) {
            case MATCH_MOVE -> new CinematicRigState(incoming.anchor(), outgoing.focus(), incoming.distance(),
                    outgoing.azimuth(), incoming.elevation(), incoming.lateralOffset(), incoming.verticalOffset(),
                    incoming.roll(), incoming.cinematicFov(), incoming.subject(), incoming.yawMode());
            case CONTINUE_ORBIT -> new CinematicRigState(incoming.anchor(), incoming.focus(), incoming.distance(),
                    forwardEquivalent(outgoing.azimuth(), incoming.azimuth()), incoming.elevation(), incoming.lateralOffset(),
                    incoming.verticalOffset(), incoming.roll(), incoming.cinematicFov(), incoming.subject(),
                    CinematicRigState.YawMode.FORWARD_ONLY);
            default -> incoming;
        };
    }

    public static double forwardEquivalent(double from, double target) {
        double result = target;
        while (result < from) result += 360.0;
        return result;
    }

    public static CinematicRigState blend(CinematicRigState from, CinematicRigState to, double amount) {
        double t = Math.max(0.0, Math.min(1.0, amount));
        double azimuth = from.azimuth() + dev.theonlytazz.idlecinematics.core.CriticallyDampedValue.shortestDelta(from.azimuth(), to.azimuth()) * t;
        OptionalDouble fov = to.cinematicFov().isPresent() ? OptionalDouble.of(from.cinematicFov().orElse(to.cinematicFov().getAsDouble())
                + (to.cinematicFov().getAsDouble() - from.cinematicFov().orElse(to.cinematicFov().getAsDouble())) * t) : OptionalDouble.empty();
        return new CinematicRigState(from.anchor().lerp(to.anchor(), t), from.focus().lerp(to.focus(), t),
                lerp(from.distance(), to.distance(), t), azimuth, lerp(from.elevation(), to.elevation(), t),
                lerp(from.lateralOffset(), to.lateralOffset(), t), lerp(from.verticalOffset(), to.verticalOffset(), t),
                lerp(from.roll(), to.roll(), t), fov, to.subject(), to.yawMode());
    }

    private static double lerp(double from, double to, double amount) { return from + (to - from) * amount; }
}
