package dev.theonlytazz.idlecinematics.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {
    public enum PathMode { ORBIT, DRIFT, ALTERNATE }

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.BooleanValue ENABLED = BUILDER.comment("Enable automatic activation")
            .define("enabled", true);
    public static final ModConfigSpec.IntValue AFK_TIMEOUT_SECONDS = BUILDER.comment("Seconds without input before activation")
            .defineInRange("afkTimeoutSeconds", 25, 5, 3600);
    public static final ModConfigSpec.DoubleValue PAN_SPEED = BUILDER.comment("Camera movement multiplier")
            .defineInRange("panSpeed", 1.0, 0.1, 4.0);
    public static final ModConfigSpec.EnumValue<PathMode> PATH_MODE = BUILDER
            .defineEnum("pathMode", PathMode.ALTERNATE);
    public static final ModConfigSpec.BooleanValue SMOOTH_TRANSITIONS = BUILDER
            .define("smoothTransitions", true);
    public static final ModConfigSpec.BooleanValue HIDE_HUD = BUILDER.define("hideHud", true);
    public static final ModConfigSpec SPEC = BUILDER.build();

    private ClientConfig() {}
}
