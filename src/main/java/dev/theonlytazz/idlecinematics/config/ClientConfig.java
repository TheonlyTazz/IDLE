package dev.theonlytazz.idlecinematics.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {
    public enum ShotMode { DYNAMIC, PLAYER_FOCUSED, ENVIRONMENT_FOCUSED, CLASSIC }

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
    public static final ModConfigSpec.BooleanValue HIDE_HUD = BUILDER.define("hideHud", true);
    public static final ModConfigSpec.BooleanValue INCLUDE_ENTITIES = BUILDER.comment("Allow shots to feature nearby living entities")
            .define("includeNearbyEntities", true);
    public static final ModConfigSpec.BooleanValue SHOW_DEBUG_PRESET = BUILDER.comment("Show the selected shot pool and preset while cinematic mode is active")
            .define("showDebugPreset", false);
    public static final ModConfigSpec SPEC = BUILDER.build();

    private ClientConfig() {}
}
