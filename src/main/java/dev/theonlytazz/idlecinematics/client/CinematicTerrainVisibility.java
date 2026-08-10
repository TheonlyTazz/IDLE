package dev.theonlytazz.idlecinematics.client;

/** Limits the broad cinematic terrain pass to the section-graph traversal call stack. */
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
        return SECTION_GRAPH_PASS.get() && ClientRuntime.isCinematicActive();
    }
}
