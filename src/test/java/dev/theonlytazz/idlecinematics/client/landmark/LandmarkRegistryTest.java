package dev.theonlytazz.idlecinematics.client.landmark;

import dev.theonlytazz.idlecinematics.api.CinematicLandmarkDefinition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class LandmarkRegistryTest {
    @Test
    void rejectsDuplicateIdentifiers() {
        LandmarkRegistry registry = new LandmarkRegistry();
        registry.register(definition("test:altar", "example:first"));
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> registry.register(definition("test:altar", "example:second")));
        assertEquals("Duplicate cinematic landmark identifier: test:altar", error.getMessage());
    }

    @Test
    void becomesImmutableWhenFrozen() {
        LandmarkRegistry registry = new LandmarkRegistry();
        registry.freeze();
        assertThrows(IllegalStateException.class, () -> registry.register(definition("test:altar", "example:first")));
    }

    @Test
    void definitionsRequireBlocksAndPositiveBounds() {
        assertThrows(IllegalArgumentException.class,
                () -> CinematicLandmarkDefinition.builder("test:empty").build());
        assertThrows(IllegalArgumentException.class,
                () -> CinematicLandmarkDefinition.builder("test:bad_radius").block("example:block").radius(0.0).build());
        assertThrows(IllegalArgumentException.class,
                () -> CinematicLandmarkDefinition.builder("test:bad_search").block("example:block").searchRadius(Double.NaN).build());
    }

    private static CinematicLandmarkDefinition definition(String id, String block) {
        return CinematicLandmarkDefinition.builder(id).block(block).build();
    }
}
