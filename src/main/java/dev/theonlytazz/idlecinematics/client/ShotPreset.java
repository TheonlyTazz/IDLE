package dev.theonlytazz.idlecinematics.client;

enum ShotPreset {
    ORBIT(false, false, false),
    TIGHT_ORBIT(false, false, false),
    WIDE_ORBIT(false, true, false),
    HERO_LOW(false, false, false),
    PROFILE(false, false, false),
    OVER_SHOULDER(false, false, false),
    OVERHEAD(false, false, false),
    PUSH_IN(false, false, false),
    SIDE_SLIDE(false, false, false),
    SKYLINE(true, true, false),
    GOLDEN_HOUR(true, true, false),
    REVEAL(true, true, false),
    CAVE_TRACK(false, false, true),
    CAVE_CLOSE(false, false, true),
    CAVE_SHOULDER(false, false, true),
    CAVE_ARC(false, false, true),
    CAVE_LOW(false, false, true),
    COMPANION(false, false, false);

    final boolean environment;
    final boolean needsSky;
    final boolean caveOnly;

    ShotPreset(boolean environment, boolean needsSky, boolean caveOnly) {
        this.environment = environment;
        this.needsSky = needsSky;
        this.caveOnly = caveOnly;
    }
}
