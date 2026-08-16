package dev.theonlytazz.idlecinematics.api;

import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record CinematicContext(
        CinematicSubject player,
        DimensionKind dimension,
        DayPhase dayPhase,
        boolean enclosed,
        boolean openSky,
        Weather weather,
        FluidState fluidState,
        int lightLevel,
        int effectiveRenderDistance,
        double ceilingClearance,
        double floorDrop,
        List<DirectionalProbe> directions,
        List<CinematicSubject> nearbySubjects,
        Optional<CinematicSubject> selectedSubject,
        Optional<CinematicSubject> terrainTarget,
        Optional<CinematicSubject> celestialTarget) {

    public CinematicContext {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(dimension, "dimension");
        Objects.requireNonNull(dayPhase, "dayPhase");
        Objects.requireNonNull(weather, "weather");
        Objects.requireNonNull(fluidState, "fluidState");
        directions = List.copyOf(directions);
        nearbySubjects = List.copyOf(nearbySubjects);
        selectedSubject = Objects.requireNonNull(selectedSubject, "selectedSubject");
        terrainTarget = Objects.requireNonNull(terrainTarget, "terrainTarget");
        celestialTarget = Objects.requireNonNull(celestialTarget, "celestialTarget");
        lightLevel = Math.max(0, Math.min(15, lightLevel));
        effectiveRenderDistance = Math.max(2, effectiveRenderDistance);
    }

    public boolean openArea() { return directions.stream().filter(probe -> probe.openDistance() >= 11.9).count() >= 4; }
    public Optional<DirectionalProbe> mostOpenDirection() {
        return directions.stream().max(java.util.Comparator.comparingDouble(DirectionalProbe::openDistance));
    }
    public Optional<DirectionalProbe> parallaxDirection() {
        return directions.stream().filter(probe -> probe.foreground().isPresent() && probe.openDistance() >= 8.0)
                .max(java.util.Comparator.comparingDouble(DirectionalProbe::openDistance));
    }

    public enum DimensionKind { OVERWORLD, NETHER, END, OTHER }
    public enum DayPhase { SUNRISE, DAY, SUNSET, NIGHT }
    public enum Weather { CLEAR, RAIN, THUNDER }
    public enum FluidState { DRY, WATER, LAVA, OTHER }

    public record DirectionalProbe(Vec3 direction, double openDistance, Optional<Vec3> foreground,
                                   Optional<Vec3> wallTarget, double floorDrop, double cameraClearance) {
        public DirectionalProbe {
            Objects.requireNonNull(direction, "direction");
            Objects.requireNonNull(foreground, "foreground");
            Objects.requireNonNull(wallTarget, "wallTarget");
        }
    }
}
