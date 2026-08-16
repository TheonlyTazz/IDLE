package dev.theonlytazz.idlecinematics.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class ActivityStateTest {
    @Test void countdownCanBeCancelled() {
        ActivityState state = new ActivityState();
        assertEquals(ActivityState.Transition.COUNTDOWN_STARTED, state.tick(true, true, 1, 60));
        assertEquals(ActivityState.Mode.COUNTDOWN, state.mode());
        assertEquals(ActivityState.Transition.EXIT, state.activity());
        assertEquals(ActivityState.Mode.WATCHING, state.mode());
    }

    @Test void forceStartAndFocusLikeActivityExitImmediately() {
        ActivityState state = new ActivityState();
        assertEquals(ActivityState.Transition.ENTER, state.forceToggle());
        state.entered();
        assertTrue(state.isCinematic());
        assertEquals(ActivityState.Transition.EXIT, state.activity());
        assertFalse(state.isCinematic());
        assertEquals(ActivityState.Mode.EXITING, state.mode());
        state.exited();
    }

    @Test void unusableWorldCleansUpAndSettingsCanSuspend() {
        ActivityState state = new ActivityState();
        state.forceToggle(); state.entered(); state.suspend();
        assertEquals(ActivityState.Mode.SUSPENDED, state.mode());
        state.resume(); assertEquals(ActivityState.Mode.ACTIVE, state.mode());
        assertEquals(ActivityState.Transition.EXIT, state.tick(false, true, 10, 3));
        assertEquals(ActivityState.Mode.EXITING, state.mode());
    }
}
