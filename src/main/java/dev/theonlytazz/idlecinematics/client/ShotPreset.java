package dev.theonlytazz.idlecinematics.client;

enum ShotPreset {
    ORBIT(false, false, false),
    WIDE_ORBIT(false, true, false),
    HERO_LOW(false, false, false),
    PROFILE(false, false, false),
    OVER_SHOULDER(false, false, false),
    OVERHEAD(false, false, false),
    SKYLINE(true, true, false),
    GOLDEN_HOUR(true, true, false),
    REVEAL(true, true, false),
    CAVE_TRACK(false, false, true),
    COMPANION(false, false, false);

    final boolean environment;
    final boolean needsSky;
    final boolean underground;

    ShotPreset(boolean environment, boolean needsSky, boolean underground) {
        this.environment = environment;
        this.needsSky = needsSky;
        this.underground = underground;
    }
}
