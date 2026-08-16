package dev.theonlytazz.idlecinematics.client;

import dev.theonlytazz.idlecinematics.config.ClientConfig;
import dev.theonlytazz.idlecinematics.config.ClientSettingsDraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleConsumer;

/** One vanilla-style settings category backed by the main screen's transactional draft. */
final class IdleSettingsSectionScreen extends Screen implements IdleSettingsView {
    private final Screen parent;
    private final ClientSettingsDraft draft;
    private final SettingsSection section;

    IdleSettingsSectionScreen(Screen parent, ClientSettingsDraft draft, SettingsSection section) {
        super(Component.translatable("idlecinematics.settings.section_title", section.title()));
        this.parent = parent;
        this.draft = draft;
        this.section = section;
    }

    @Override protected void init() {
        int left = width / 2 - 155;
        int right = width / 2 + 5;
        int y = 76;
        switch (section) {
            case GENERAL -> {
                toggle(left, y, "enabled", draft.enabled, value -> draft.enabled = value);
                slider(right, y, "timeout", draft.timeoutSeconds, 5, 300, 1, value -> draft.timeoutSeconds = (int) value, "s");
                toggle(left, y + 24, "countdown", draft.countdownEnabled, value -> draft.countdownEnabled = value);
                slider(right, y + 24, "countdown_seconds", draft.countdownSeconds, 0, 10, 1, value -> draft.countdownSeconds = (int) value, "s");
                toggle(left, y + 48, "focus_regain", draft.exitOnFocusRegain, value -> draft.exitOnFocusRegain = value);
            }
            case CAMERA -> {
                addRenderableWidget(CycleButton.<ClientConfig.ShotMode>builder(value -> Component.translatable("idlecinematics.shot_mode." + value.name().toLowerCase(Locale.ROOT))).withInitialValue(draft.shotMode)
                        .withValues(List.of(ClientConfig.ShotMode.values())).create(left, y, 150, 20, label("shot_mode"), (button, value) -> draft.shotMode = value));
                slider(right, y, "shot_duration", draft.shotDurationSeconds, 5, 30, 1, value -> draft.shotDurationSeconds = (int) value, "s");
                slider(left, y + 24, "speed", draft.panSpeed, 0.1, 4, 0.1, value -> draft.panSpeed = value, "x");
                slider(right, y + 24, "distance", draft.cameraDistance, 0.6, 1.6, 0.1, value -> draft.cameraDistance = value, "x");
                toggle(left, y + 48, "smoothing", draft.smoothTransitions, value -> draft.smoothTransitions = value);
                slider(right, y + 48, "transition", draft.transitionIntensity, 0, 2, 0.1, value -> draft.transitionIntensity = value, "x");
            }
            case SCENES -> {
                toggle(left, y, "entities", draft.includeEntities, value -> draft.includeEntities = value);
                addRenderableWidget(Button.builder(label("configure_scenes"),
                        button -> minecraft.setScreen(new SceneSelectionScreen(this, draft)))
                        .bounds(right, y, 150, 20).build());
                toggle(left, y + 24, "player_pool", draft.playerPool, value -> draft.playerPool = value);
                toggle(right, y + 24, "landscape_pool", draft.landscapePool, value -> draft.landscapePool = value);
                toggle(left, y + 48, "entity_pool", draft.entityPool, value -> draft.entityPool = value);
                toggle(right, y + 48, "celestial_pool", draft.celestialPool, value -> draft.celestialPool = value);
            }
            case HUD -> {
                toggle(left, y, "hide_hud", draft.hideHud, value -> draft.hideHud = value);
                toggle(right, y, "timer_title", draft.showTimerTitle, value -> draft.showTimerTitle = value);
                toggle(left, y + 24, "timer", draft.showTimer, value -> draft.showTimer = value);
                slider(right, y + 24, "hud_scale", draft.hudScale, 0.5, 2, 0.1, value -> draft.hudScale = value, "x");
                addRenderableWidget(CycleButton.<ClientConfig.HudAnchor>builder(value -> Component.literal(value.name().toLowerCase(Locale.ROOT).replace('_', ' '))).withInitialValue(draft.hudAnchor)
                        .withValues(List.of(ClientConfig.HudAnchor.values())).create(left, y + 48, 150, 20, label("hud_anchor"), (button, value) -> draft.hudAnchor = value));
            }
            case DEBUG -> toggle(left, y, "debug_preset", draft.debug, value -> draft.debug = value);
            case PROFILES -> {
                toggle(left, y, "fps_profile", draft.fpsCapEnabled, value -> draft.fpsCapEnabled = value);
                slider(right, y, "fps_cap", draft.fpsCap, 10, 260, 5, value -> draft.fpsCap = (int) value, " fps");
                toggle(left, y + 24, "fov_profile", draft.fovEnabled, value -> draft.fovEnabled = value);
                slider(right, y + 24, "fov", draft.fov, 30, 110, 1, value -> draft.fov = (int) value, "°");
                toggle(left, y + 48, "audio_profile", draft.audioEnabled, value -> draft.audioEnabled = value);
                slider(right, y + 48, "volume", draft.masterVolume * 100, 0, 100, 5, value -> draft.masterVolume = value / 100, "%");
            }
        }
        int footerY = height - 28;
        addRenderableWidget(Button.builder(Component.translatable("idlecinematics.settings.reset_section"), button -> resetSection())
                .bounds(width / 2 - 155, footerY, 150, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(width / 2 + 5, footerY, 150, 20).build());
    }

    private void toggle(int x, int y, String key, boolean initial, java.util.function.Consumer<Boolean> setter) {
        addRenderableWidget(CycleButton.onOffBuilder(initial).create(x, y, 150, 20, label(key), (button, value) -> setter.accept(value)));
    }

    private void slider(int x, int y, String key, double initial, double min, double max, double step, DoubleConsumer setter, String suffix) {
        addRenderableWidget(new ValueSlider(x, y, label(key), initial, min, max, step, setter, suffix));
    }

    private static Component label(String key) { return Component.translatable("idlecinematics.settings." + key); }

    private void resetSection() {
        switch (section) {
            case GENERAL -> draft.resetGeneralDefaults();
            case CAMERA -> draft.resetCameraDefaults();
            case SCENES -> draft.resetSceneDefaults();
            case HUD -> draft.resetHudDefaults();
            case DEBUG -> draft.resetDebugDefaults();
            case PROFILES -> draft.resetProfileDefaults();
        }
        rebuildWidgets();
    }

    @Override public void onClose() { minecraft.setScreen(parent); }

    @Override protected void repositionElements() { rebuildWidgets(); }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        IdleSettingsScreen.renderVanillaPanel(graphics, width, height);
        graphics.drawCenteredString(font, title, width / 2, 28, 0xFFFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private static final class ValueSlider extends AbstractSliderButton {
        private final Component label;
        private final double min;
        private final double max;
        private final double step;
        private final DoubleConsumer setter;
        private final String suffix;

        private ValueSlider(int x, int y, Component label, double initial, double min, double max, double step, DoubleConsumer setter, String suffix) {
            super(x, y, 150, 20, CommonComponents.EMPTY, (initial - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.step = step;
            this.setter = setter;
            this.suffix = suffix;
            updateMessage();
        }

        private double selected() { return Math.round((min + value * (max - min)) / step) * step; }

        @Override protected void updateMessage() {
            double selected = selected();
            String text = step >= 1.0 ? Integer.toString((int) selected) : String.format(Locale.ROOT, "%.1f", selected);
            setMessage(Component.empty().append(label).append(": " + text + suffix));
        }

        @Override protected void applyValue() { setter.accept(selected()); }
    }
}
