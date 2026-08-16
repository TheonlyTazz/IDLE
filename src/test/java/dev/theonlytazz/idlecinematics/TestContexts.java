package dev.theonlytazz.idlecinematics;

import dev.theonlytazz.idlecinematics.api.CinematicContext;
import dev.theonlytazz.idlecinematics.api.CinematicSubject;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class TestContexts {
    private TestContexts() {}
    public static CinematicContext context(boolean cave, CinematicContext.DimensionKind dimension,
                                           CinematicContext.DayPhase phase, CinematicContext.Weather weather,
                                           boolean openSky, int renderDistance, Optional<CinematicSubject> entity) {
        CinematicSubject player = new CinematicSubject(CinematicSubject.Type.PLAYER,
                Optional.of(new UUID(0, 1)), Vec3.ZERO, 1.8, 0.0);
        List<CinematicContext.DirectionalProbe> probes = new ArrayList<>();
        for (int index = 0; index < 8; index++) {
            double angle = index * Math.PI / 4.0;
            probes.add(new CinematicContext.DirectionalProbe(new Vec3(Math.cos(angle), 0, Math.sin(angle)),
                    cave ? 2.0 : 16.0, Optional.empty(), Optional.empty(), 1.0, cave ? 2.0 : 12.0));
        }
        CinematicSubject terrain = new CinematicSubject(CinematicSubject.Type.TERRAIN, Optional.empty(), new Vec3(10, 0, 0), 2, 0);
        CinematicSubject celestial = new CinematicSubject(CinematicSubject.Type.CELESTIAL, Optional.empty(), new Vec3(10, 20, 0), 4, 0);
        return new CinematicContext(player, dimension, phase, cave, openSky, weather, CinematicContext.FluidState.DRY,
                12, renderDistance, cave ? 3 : 16, 1, probes, entity.stream().toList(), entity,
                Optional.of(terrain), openSky ? Optional.of(celestial) : Optional.empty());
    }
}
