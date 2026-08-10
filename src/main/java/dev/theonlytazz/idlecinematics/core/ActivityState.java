package dev.theonlytazz.idlecinematics.core;

public final class ActivityState {
    public enum Mode { WATCHING, CINEMATIC }

    private Mode mode = Mode.WATCHING;
    private int idleTicks;

    public boolean tick(boolean usableWorld, boolean enabled, int timeoutTicks) {
        if (!usableWorld || !enabled) {
            reset();
            return false;
        }
        if (mode == Mode.WATCHING && ++idleTicks >= timeoutTicks) {
            mode = Mode.CINEMATIC;
            return true;
        }
        return false;
    }

    public boolean activity() {
        boolean wasActive = mode == Mode.CINEMATIC;
        reset();
        return wasActive;
    }

    public void forceStart() { mode = Mode.CINEMATIC; }
    public void reset() { mode = Mode.WATCHING; idleTicks = 0; }
    public boolean isCinematic() { return mode == Mode.CINEMATIC; }
    public int idleTicks() { return idleTicks; }
}
