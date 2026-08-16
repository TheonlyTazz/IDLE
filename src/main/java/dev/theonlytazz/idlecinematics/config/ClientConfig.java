package dev.theonlytazz.idlecinematics.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {
    public enum ShotMode { DYNAMIC, PLAYER_FOCUSED, ENVIRONMENT_FOCUSED, CLASSIC }
    public enum HudPolicy { SHOW_ALL, HIDE_VANILLA }
    public enum HudAnchor { CENTER, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER.comment("Enable automatic activation")
            .define("enabled", true);
    public static final ModConfigSpec.IntValue AFK_TIMEOUT_SECONDS = BUILDER.comment("Seconds without input before activation")
            .defineInRange("afkTimeoutSeconds", 25, 5, 3600);
    public static final ModConfigSpec.DoubleValue PAN_SPEED = BUILDER.comment("Camera movement multiplier")
            .defineInRange("panSpeed", 1.0, 0.1, 4.0);
    public static final ModConfigSpec.IntValue SHOT_DURATION_SECONDS = BUILDER.comment("Seconds before choosing a new shot")
            .defineInRange("shotDurationSeconds", 9, 5, 30);
    public static final ModConfigSpec.EnumValue<ShotMode> SHOT_MODE = BUILDER.comment("How the shot director selects compositions")
            .defineEnum("shotMode", ShotMode.DYNAMIC);
    public static final ModConfigSpec.DoubleValue CAMERA_DISTANCE = BUILDER.comment("Camera distance multiplier")
            .defineInRange("cameraDistance", 1.0, 0.6, 1.6);
    public static final ModConfigSpec.BooleanValue SMOOTH_TRANSITIONS = BUILDER
            .define("smoothTransitions", true);
    public static final ModConfigSpec.DoubleValue TRANSITION_INTENSITY = BUILDER.comment("Transition duration multiplier; shot speed is unaffected")
            .defineInRange("transitionIntensity", 1.0, 0.0, 2.0);
    public static final ModConfigSpec.BooleanValue HIDE_HUD = BUILDER.define("hideHud", true);
    public static final ModConfigSpec.BooleanValue INCLUDE_ENTITIES = BUILDER.comment("Allow shots to feature nearby living entities")
            .define("includeNearbyEntities", true);
    public static final ModConfigSpec.BooleanValue SHOW_DEBUG_PRESET = BUILDER.comment("Show the selected shot pool and preset while cinematic mode is active")
            .define("showDebugPreset", false);
    public static final ModConfigSpec.BooleanValue COUNTDOWN_ENABLED = BUILDER.define("countdownEnabled", true);
    public static final ModConfigSpec.IntValue COUNTDOWN_SECONDS = BUILDER.defineInRange("countdownSeconds", 3, 0, 10);
    public static final ModConfigSpec.BooleanValue EXIT_ON_FOCUS_REGAIN = BUILDER.define("exitOnFocusRegain", true);
    public static final ModConfigSpec.BooleanValue SHOW_TIMER_TITLE = BUILDER.define("showTimerTitle", true);
    public static final ModConfigSpec.BooleanValue SHOW_AFK_TIMER = BUILDER.define("showAfkTimer", true);
    public static final ModConfigSpec.DoubleValue HUD_SCALE = BUILDER.defineInRange("hudScale", 1.0, 0.5, 2.0);
    public static final ModConfigSpec.EnumValue<HudAnchor> HUD_ANCHOR = BUILDER.defineEnum("hudAnchor", HudAnchor.TOP_RIGHT);
    public static final ModConfigSpec.BooleanValue ENABLE_NEW_MOTIONS = BUILDER.define("enableNewMotionFamilies", true);
    public static final ModConfigSpec.BooleanValue PLAYER_POOL_ENABLED = BUILDER.define("playerShotPoolEnabled", true);
    public static final ModConfigSpec.BooleanValue LANDSCAPE_POOL_ENABLED = BUILDER.define("landscapeShotPoolEnabled", true);
    public static final ModConfigSpec.BooleanValue ENTITY_POOL_ENABLED = BUILDER.define("entityShotPoolEnabled", true);
    public static final ModConfigSpec.BooleanValue CELESTIAL_POOL_ENABLED = BUILDER.define("celestialShotPoolEnabled", true);
    public static final ModConfigSpec.BooleanValue FPS_CAP_ENABLED = BUILDER.define("fpsCapEnabled", false);
    public static final ModConfigSpec.IntValue FPS_CAP = BUILDER.defineInRange("fpsCap", 30, 10, 260);
    public static final ModConfigSpec.BooleanValue CINEMATIC_FOV_ENABLED = BUILDER.define("cinematicFovEnabled", false);
    public static final ModConfigSpec.IntValue CINEMATIC_FOV = BUILDER.defineInRange("cinematicFov", 55, 30, 110);
    public static final ModConfigSpec.BooleanValue AUDIO_PROFILE_ENABLED = BUILDER.define("audioProfileEnabled", false);
    public static final ModConfigSpec.DoubleValue MASTER_VOLUME = BUILDER.defineInRange("cinematicMasterVolume", 0.35, 0.0, 1.0);
    public static final ModConfigSpec SPEC = BUILDER.build();

    private ClientConfig() {}
}
