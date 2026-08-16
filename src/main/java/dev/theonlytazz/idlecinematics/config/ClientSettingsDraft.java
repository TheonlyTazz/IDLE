package dev.theonlytazz.idlecinematics.config;

import java.util.LinkedHashSet;
import java.util.Set;

/** Transactional settings model. Widgets never mutate live config before Apply. */
public final class ClientSettingsDraft {
    public boolean enabled;
    public int timeoutSeconds;
    public double panSpeed;
    public int shotDurationSeconds;
    public ClientConfig.ShotMode shotMode;
    public double cameraDistance;
    public boolean smoothTransitions;
    public double transitionIntensity;
    public boolean hideHud;
    public boolean includeEntities;
    public boolean debug;
    public boolean countdownEnabled;
    public int countdownSeconds;
    public boolean exitOnFocusRegain;
    public boolean showTimerTitle;
    public boolean showTimer;
    public double hudScale;
    public ClientConfig.HudAnchor hudAnchor;
    public boolean newMotions;
    public final Set<String> disabledPresets = new LinkedHashSet<>();
    public boolean playerPool;
    public boolean landscapePool;
    public boolean entityPool;
    public boolean celestialPool;
    public boolean fpsCapEnabled;
    public int fpsCap;
    public boolean fovEnabled;
    public int fov;
    public boolean audioEnabled;
    public double masterVolume;

    public static ClientSettingsDraft snapshot() {
        ClientSettingsDraft draft = new ClientSettingsDraft();
        draft.enabled = ClientConfig.ENABLED.getAsBoolean();
        draft.timeoutSeconds = ClientConfig.AFK_TIMEOUT_SECONDS.getAsInt();
        draft.panSpeed = ClientConfig.PAN_SPEED.getAsDouble();
        draft.shotDurationSeconds = ClientConfig.SHOT_DURATION_SECONDS.getAsInt();
        draft.shotMode = ClientConfig.SHOT_MODE.get();
        draft.cameraDistance = ClientConfig.CAMERA_DISTANCE.getAsDouble();
        draft.smoothTransitions = ClientConfig.SMOOTH_TRANSITIONS.getAsBoolean();
        draft.transitionIntensity = ClientConfig.TRANSITION_INTENSITY.getAsDouble();
        draft.hideHud = ClientConfig.HIDE_HUD.getAsBoolean();
        draft.includeEntities = ClientConfig.INCLUDE_ENTITIES.getAsBoolean();
        draft.debug = ClientConfig.SHOW_DEBUG_PRESET.getAsBoolean();
        draft.countdownEnabled = ClientConfig.COUNTDOWN_ENABLED.getAsBoolean();
        draft.countdownSeconds = ClientConfig.COUNTDOWN_SECONDS.getAsInt();
        draft.exitOnFocusRegain = ClientConfig.EXIT_ON_FOCUS_REGAIN.getAsBoolean();
        draft.showTimerTitle = ClientConfig.SHOW_TIMER_TITLE.getAsBoolean();
        draft.showTimer = ClientConfig.SHOW_AFK_TIMER.getAsBoolean();
        draft.hudScale = ClientConfig.HUD_SCALE.getAsDouble();
        draft.hudAnchor = ClientConfig.HUD_ANCHOR.get();
        draft.newMotions = ClientConfig.ENABLE_NEW_MOTIONS.getAsBoolean();
        draft.disabledPresets.addAll(PresetPreferences.parseDisabled(ClientConfig.DISABLED_PRESETS.get()));
        draft.playerPool = ClientConfig.PLAYER_POOL_ENABLED.getAsBoolean();
        draft.landscapePool = ClientConfig.LANDSCAPE_POOL_ENABLED.getAsBoolean();
        draft.entityPool = ClientConfig.ENTITY_POOL_ENABLED.getAsBoolean();
        draft.celestialPool = ClientConfig.CELESTIAL_POOL_ENABLED.getAsBoolean();
        draft.fpsCapEnabled = ClientConfig.FPS_CAP_ENABLED.getAsBoolean();
        draft.fpsCap = ClientConfig.FPS_CAP.getAsInt();
        draft.fovEnabled = ClientConfig.CINEMATIC_FOV_ENABLED.getAsBoolean();
        draft.fov = ClientConfig.CINEMATIC_FOV.getAsInt();
        draft.audioEnabled = ClientConfig.AUDIO_PROFILE_ENABLED.getAsBoolean();
        draft.masterVolume = ClientConfig.MASTER_VOLUME.getAsDouble();
        return draft;
    }

    public void resetDefaults() {
        resetGeneralDefaults();
        resetCameraDefaults();
        resetSceneDefaults();
        resetHudDefaults();
        resetDebugDefaults();
        resetProfileDefaults();
    }

    public void resetGeneralDefaults() {
        enabled = true; timeoutSeconds = 25;
        countdownEnabled = true; countdownSeconds = 3; exitOnFocusRegain = false;
    }

    public void resetCameraDefaults() {
        panSpeed = 1.0; shotDurationSeconds = 9; shotMode = ClientConfig.ShotMode.DYNAMIC;
        cameraDistance = 1.0; smoothTransitions = true; transitionIntensity = 1.0;
    }

    public void resetSceneDefaults() {
        includeEntities = true; resetPresetDefaults();
        playerPool = true; landscapePool = true; entityPool = true; celestialPool = true;
    }

    public void resetPresetDefaults() {
        newMotions = true; disabledPresets.clear();
    }

    public void resetHudDefaults() {
        hideHud = true;
        showTimerTitle = false; showTimer = true; hudScale = 1.0; hudAnchor = ClientConfig.HudAnchor.TOP_LEFT;
    }

    public void resetDebugDefaults() { debug = false; }

    public void resetProfileDefaults() {
        fpsCapEnabled = false; fpsCap = 30; fovEnabled = false; fov = 55;
        audioEnabled = false; masterVolume = 0.35;
    }

    public void apply() {
        commitTo(draft -> draft.writeLiveConfig());
    }

    public boolean sceneEnabled(String id) {
        return PresetPreferences.isEnabled(id, disabledPresets, newMotions);
    }

    public void setSceneEnabled(String id, boolean enabled) {
        PresetPreferences.migrateLegacyChoice(disabledPresets, newMotions);
        newMotions = true;
        if (enabled) disabledPresets.remove(id); else disabledPresets.add(id);
    }

    public boolean legacyPoolEnabled(String pool) {
        return PresetPreferences.isPoolEnabled(pool, playerPool, landscapePool, entityPool, celestialPool);
    }

    /** Once represented by individual scene choices, legacy pool keys remain enabled for config compatibility. */
    public void finishLegacyPoolMigration() {
        playerPool = true;
        landscapePool = true;
        entityPool = true;
        celestialPool = true;
    }

    public void commitTo(java.util.function.Consumer<ClientSettingsDraft> sink) {
        validate();
        sink.accept(this);
    }

    private void writeLiveConfig() {
        ClientConfig.ENABLED.set(enabled); ClientConfig.AFK_TIMEOUT_SECONDS.set(timeoutSeconds);
        ClientConfig.PAN_SPEED.set(panSpeed); ClientConfig.SHOT_DURATION_SECONDS.set(shotDurationSeconds);
        ClientConfig.SHOT_MODE.set(shotMode); ClientConfig.CAMERA_DISTANCE.set(cameraDistance);
        ClientConfig.SMOOTH_TRANSITIONS.set(smoothTransitions); ClientConfig.TRANSITION_INTENSITY.set(transitionIntensity);
        ClientConfig.HIDE_HUD.set(hideHud); ClientConfig.INCLUDE_ENTITIES.set(includeEntities);
        ClientConfig.SHOW_DEBUG_PRESET.set(debug); ClientConfig.COUNTDOWN_ENABLED.set(countdownEnabled);
        ClientConfig.COUNTDOWN_SECONDS.set(countdownSeconds); ClientConfig.EXIT_ON_FOCUS_REGAIN.set(exitOnFocusRegain);
        ClientConfig.SHOW_TIMER_TITLE.set(showTimerTitle); ClientConfig.SHOW_AFK_TIMER.set(showTimer);
        ClientConfig.HUD_SCALE.set(hudScale); ClientConfig.HUD_ANCHOR.set(hudAnchor);
        ClientConfig.ENABLE_NEW_MOTIONS.set(newMotions); ClientConfig.FPS_CAP_ENABLED.set(fpsCapEnabled);
        ClientConfig.DISABLED_PRESETS.set(PresetPreferences.encodeDisabled(disabledPresets));
        ClientConfig.PLAYER_POOL_ENABLED.set(playerPool); ClientConfig.LANDSCAPE_POOL_ENABLED.set(landscapePool);
        ClientConfig.ENTITY_POOL_ENABLED.set(entityPool); ClientConfig.CELESTIAL_POOL_ENABLED.set(celestialPool);
        ClientConfig.FPS_CAP.set(fpsCap); ClientConfig.CINEMATIC_FOV_ENABLED.set(fovEnabled);
        ClientConfig.CINEMATIC_FOV.set(fov); ClientConfig.AUDIO_PROFILE_ENABLED.set(audioEnabled);
        ClientConfig.MASTER_VOLUME.set(masterVolume); ClientConfig.SPEC.save();
    }

    private void validate() {
        timeoutSeconds = clamp(timeoutSeconds, 5, 3600); shotDurationSeconds = clamp(shotDurationSeconds, 5, 30);
        countdownSeconds = clamp(countdownSeconds, 0, 10); fpsCap = clamp(fpsCap, 10, 260); fov = clamp(fov, 30, 110);
        panSpeed = clamp(panSpeed, 0.1, 4.0); cameraDistance = clamp(cameraDistance, 0.6, 1.6);
        transitionIntensity = clamp(transitionIntensity, 0.0, 2.0); hudScale = clamp(hudScale, 0.5, 2.0);
        masterVolume = clamp(masterVolume, 0.0, 1.0);
    }
    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
    private static double clamp(double value, double min, double max) { return Math.max(min, Math.min(max, Double.isFinite(value) ? value : min)); }
}
