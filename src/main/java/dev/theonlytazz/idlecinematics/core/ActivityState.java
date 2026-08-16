package dev.theonlytazz.idlecinematics.core;

/** Pure AFK state machine. Rendering and Minecraft screen changes live in the client adapter. */
public final class ActivityState {
    public enum Mode { WATCHING, COUNTDOWN, ENTERING, ACTIVE, EXITING, SUSPENDED }

    private Mode mode = Mode.WATCHING;
    private int idleTicks;
    private int countdownTicks;
    private long activeTicks;

    public Transition tick(boolean usableWorld, boolean enabled, int timeoutTicks, int configuredCountdownTicks) {
        if (!usableWorld || !enabled) return stop();
        return switch (mode) {
            case WATCHING -> {
                if (++idleTicks >= Math.max(1, timeoutTicks)) {
                    countdownTicks = Math.max(0, configuredCountdownTicks);
                    mode = countdownTicks == 0 ? Mode.ENTERING : Mode.COUNTDOWN;
                    yield mode == Mode.ENTERING ? Transition.ENTER : Transition.COUNTDOWN_STARTED;
                }
                yield Transition.NONE;
            }
            case COUNTDOWN -> {
                if (--countdownTicks <= 0) {
                    mode = Mode.ENTERING;
                    yield Transition.ENTER;
                }
                yield Transition.NONE;
            }
            case ENTERING -> Transition.NONE;
            case ACTIVE -> { activeTicks++; yield Transition.NONE; }
            case EXITING -> { reset(); yield Transition.NONE; }
            case SUSPENDED -> Transition.NONE;
        };
    }

    public Transition activity() {
        if (mode == Mode.COUNTDOWN) { reset(); return Transition.EXIT; }
        if (isCinematic()) { mode = Mode.EXITING; idleTicks = 0; countdownTicks = 0; return Transition.EXIT; }
        return Transition.NONE;
    }

    public Transition forceToggle() {
        if (isCinematic() || mode == Mode.COUNTDOWN) return stop();
        mode = Mode.ENTERING;
        idleTicks = 0;
        return Transition.ENTER;
    }

    public void entered() { if (mode == Mode.ENTERING) mode = Mode.ACTIVE; }
    public void exited() { if (mode == Mode.EXITING) reset(); }
    public void suspend() { if (mode == Mode.ACTIVE) mode = Mode.SUSPENDED; }
    public void resume() { if (mode == Mode.SUSPENDED) mode = Mode.ACTIVE; }

    public Transition stop() {
        boolean running = isCinematic() || mode == Mode.COUNTDOWN || mode == Mode.ENTERING;
        if (isCinematic() || mode == Mode.ENTERING) mode = Mode.EXITING; else reset();
        return running ? Transition.EXIT : Transition.NONE;
    }

    public void reset() { mode = Mode.WATCHING; idleTicks = 0; countdownTicks = 0; activeTicks = 0; }
    public boolean isCinematic() { return mode == Mode.ENTERING || mode == Mode.ACTIVE || mode == Mode.SUSPENDED; }
    public boolean isRendering() { return mode == Mode.ACTIVE; }
    public Mode mode() { return mode; }
    public int idleTicks() { return idleTicks; }
    public int countdownTicks() { return Math.max(0, countdownTicks); }
    public long activeTicks() { return activeTicks; }

    public enum Transition { NONE, COUNTDOWN_STARTED, ENTER, EXIT }
}
