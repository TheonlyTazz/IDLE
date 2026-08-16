package dev.theonlytazz.idlecinematics.client;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;

/** Keeps the cinematic lifecycle suspended while vanilla renders the reset confirmation. */
final class IdleResetConfirmScreen extends ConfirmScreen implements IdleSettingsView {
    IdleResetConfirmScreen(BooleanConsumer callback, Component title, Component message) {
        super(callback, title, message);
    }
}
