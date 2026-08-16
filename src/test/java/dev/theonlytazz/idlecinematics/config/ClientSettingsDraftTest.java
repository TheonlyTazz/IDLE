package dev.theonlytazz.idlecinematics.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class ClientSettingsDraftTest {
    @Test void resetOnlyMutatesDraftDefaults() {
        ClientSettingsDraft draft = new ClientSettingsDraft();
        draft.enabled = false; draft.timeoutSeconds = 999; draft.fpsCapEnabled = true;
        draft.resetDefaults();
        assertTrue(draft.enabled); assertEquals(25, draft.timeoutSeconds); assertFalse(draft.fpsCapEnabled);
        assertEquals(30, draft.fpsCap); assertEquals(55, draft.fov); assertEquals(0.35, draft.masterVolume);
    }
}
