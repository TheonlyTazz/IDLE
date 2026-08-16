package dev.theonlytazz.idlecinematics.client;

import net.minecraft.network.chat.Component;
import java.util.Locale;

enum SettingsSection {
    GENERAL, CAMERA, SCENES, HUD, DEBUG, PROFILES;

    Component title() {
        return Component.translatable("idlecinematics.settings.page." + name().toLowerCase(Locale.ROOT));
    }
}
