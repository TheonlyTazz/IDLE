package dev.theonlytazz.idlecinematics.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.atomic.AtomicBoolean;

final class ClientSettingsDraftTest {
    @Test void resetOnlyMutatesDraftDefaults() {
        ClientSettingsDraft draft = new ClientSettingsDraft();
        draft.enabled = false; draft.timeoutSeconds = 999; draft.fpsCapEnabled = true;
        draft.resetDefaults();
        assertTrue(draft.enabled); assertEquals(25, draft.timeoutSeconds); assertFalse(draft.fpsCapEnabled);
        assertEquals(30, draft.fpsCap); assertEquals(55, draft.fov); assertEquals(0.35, draft.masterVolume);
        assertFalse(draft.exitOnFocusRegain); assertFalse(draft.showTimerTitle);
        assertEquals(ClientConfig.HudAnchor.TOP_LEFT, draft.hudAnchor);
    }

    @Test void applyValidatesAndCommitsWhileCancelDoesNothing() {
        ClientSettingsDraft draft = new ClientSettingsDraft(); draft.resetDefaults();
        draft.timeoutSeconds = 99999;
        AtomicBoolean committed = new AtomicBoolean();
        draft.commitTo(value -> { committed.set(true); assertEquals(3600, value.timeoutSeconds); });
        assertTrue(committed.get());
        committed.set(false); // A cancelled screen simply discards its draft and never invokes commitTo.
        assertFalse(committed.get());
    }

    @Test void individualSceneChoiceMigratesTheLegacyGroupWithoutEnablingItsPeers() {
        ClientSettingsDraft draft = new ClientSettingsDraft();
        draft.resetDefaults();
        draft.newMotions = false;

        draft.setSceneEnabled("idlecinematics:terrain_scout", true);

        assertTrue(draft.newMotions);
        assertTrue(draft.sceneEnabled("idlecinematics:terrain_scout"));
        assertFalse(draft.sceneEnabled("idlecinematics:entity_portrait"));
        assertTrue(draft.sceneEnabled("example_addon:custom_scene"));
    }

    @Test void categoryResetDoesNotChangeOtherPages() {
        ClientSettingsDraft draft = new ClientSettingsDraft();
        draft.resetDefaults();
        draft.enabled = false;
        draft.panSpeed = 3.5;
        draft.cameraDistance = 1.6;
        draft.hideHud = false;

        draft.resetCameraDefaults();

        assertFalse(draft.enabled);
        assertEquals(1.0, draft.panSpeed);
        assertEquals(1.0, draft.cameraDistance);
        assertFalse(draft.hideHud);
    }

    @Test void presetResetDoesNotChangeScenePoolChoices() {
        ClientSettingsDraft draft = new ClientSettingsDraft();
        draft.resetDefaults();
        draft.playerPool = false;
        draft.setSceneEnabled("idlecinematics:orbit", false);

        draft.resetPresetDefaults();

        assertTrue(draft.sceneEnabled("idlecinematics:orbit"));
        assertFalse(draft.playerPool);
    }

    @Test void legacyPoolChoicesCanBeMaterializedIntoIndividualScenes() {
        ClientSettingsDraft draft = new ClientSettingsDraft();
        draft.resetDefaults();
        draft.playerPool = false;

        assertFalse(draft.legacyPoolEnabled("player"));
        assertFalse(draft.legacyPoolEnabled("cave"));
        assertTrue(draft.legacyPoolEnabled("landmark"));

        draft.setSceneEnabled("idlecinematics:orbit", false);
        draft.finishLegacyPoolMigration();

        assertTrue(draft.playerPool);
        assertFalse(draft.sceneEnabled("idlecinematics:orbit"));
    }

    @Test void sceneResetRestoresLegacyEntityAnalysisForIndividualEntityScenes() {
        ClientSettingsDraft draft = new ClientSettingsDraft();
        draft.resetDefaults();
        draft.includeEntities = false;
        draft.setSceneEnabled("idlecinematics:entity_portrait", false);

        draft.resetSceneDefaults();

        assertTrue(draft.includeEntities);
        assertTrue(draft.sceneEnabled("idlecinematics:entity_portrait"));
    }
}
