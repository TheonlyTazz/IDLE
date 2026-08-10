package dev.theonlytazz.idlecinematics.client;

import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record SceneContext(
        Vec3 playerFocus,
        Optional<Vec3> nearbyEntityFocus,
        DimensionKind dimension,
        DayPhase dayPhase,
        boolean openSky,
        boolean enclosed,
        int openDirections,
        double ceilingClearance
) {
    public enum DimensionKind { OVERWORLD, NETHER, END, OTHER }
    public enum DayPhase { SUNRISE, DAY, SUNSET, NIGHT }

    public boolean openArea() {
        return openDirections >= 3;
    }
}
