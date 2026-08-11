package dev.theonlytazz.idlecinematics.client.render;

import dev.theonlytazz.idlecinematics.client.ClientRuntime;

/** Keeps terrain frustum checks permissive for the duration of a cinematic shot. */
public final class CinematicTerrainVisibility {
    private static final ThreadLocal<Boolean> SECTION_GRAPH_PASS = ThreadLocal.withInitial(() -> false);

    private CinematicTerrainVisibility() {}

    public static void beginSectionGraphPass() {
        SECTION_GRAPH_PASS.set(true);
    }

    public static void endSectionGraphPass() {
        SECTION_GRAPH_PASS.remove();
    }

    public static boolean shouldKeepTerrainReady() {
        // The section graph and the later chunk render pass do not share one call stack.
        // Restricting this to SECTION_GRAPH_PASS leaves later frustum checks able to cull
        // terrain that the cinematic camera has moved toward or behind.
        return ClientRuntime.isCinematicActive();
    }
}
