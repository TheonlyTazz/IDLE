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

public final class IdleSettingsScreen extends Screen {
    private enum Page { GENERAL, CAMERA, SCENES, HUD, DEBUG, PROFILES }
    private final Screen parent;
    private final ClientSettingsDraft draft;
    private Page page = Page.GENERAL;
    private boolean applied;

    public IdleSettingsScreen(Screen parent) {
        super(Component.translatable("idlecinematics.settings.title"));
        this.parent = parent;
        this.draft = ClientSettingsDraft.snapshot();
    }

    @Override protected void init() {
        int left = width / 2 - 155;
        int right = width / 2 + 5;
        int y = 50;
        switch (page) {
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
        int navigationY = Math.min(height - 52, 146);
        Button previous = Button.builder(Component.translatable("idlecinematics.settings.previous"), button -> changePage(-1))
                .bounds(width / 2 - 155, navigationY, 96, 20).build();
        previous.active = page.ordinal() > 0;
        addRenderableWidget(previous);
        Button next = Button.builder(Component.translatable("idlecinematics.settings.next"), button -> changePage(1))
                .bounds(width / 2 + 59, navigationY, 96, 20).build();
        next.active = page.ordinal() < Page.values().length - 1;
        addRenderableWidget(next);

        int actionsY = navigationY + 24;
        addRenderableWidget(Button.builder(Component.translatable("idlecinematics.settings.reset"), button -> { draft.resetDefaults(); rebuild(); })
                .bounds(width / 2 - 155, actionsY, 96, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> closeCanceled()).bounds(width / 2 - 49, actionsY, 96, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("idlecinematics.settings.apply"), button -> applyAndClose()).bounds(width / 2 + 57, actionsY, 96, 20).build());
    }

    private void changePage(int direction) {
        int selected = Math.max(0, Math.min(Page.values().length - 1, page.ordinal() + direction));
        if (selected != page.ordinal()) { page = Page.values()[selected]; rebuild(); }
    }
    private void rebuild() { clearWidgets(); init(); }
    private void toggle(int x, int y, String key, boolean initial, java.util.function.Consumer<Boolean> setter) {
        addRenderableWidget(CycleButton.onOffBuilder(initial).create(x, y, 150, 20, label(key), (button, value) -> setter.accept(value)));
    }
    private void slider(int x, int y, String key, double initial, double min, double max, double step, DoubleConsumer setter, String suffix) {
        addRenderableWidget(new ValueSlider(x, y, label(key), initial, min, max, step, setter, suffix));
    }
    private static Component label(String key) { return Component.translatable("idlecinematics.settings." + key); }

    private void applyAndClose() { draft.apply(); applied = true; ClientRuntime.onSettingsApplied(); minecraft.setScreen(parent); }
    private void closeCanceled() { minecraft.setScreen(parent); }
    @Override public void onClose() { if (!applied) closeCanceled(); }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, pageTitle(), width / 2, 18, 0xFFFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private Component pageTitle() {
        return Component.translatable("idlecinematics.settings.page_header",
                Component.translatable("idlecinematics.settings.page." + page.name().toLowerCase(Locale.ROOT)),
                page.ordinal() + 1, Page.values().length);
    }

    private static final class ValueSlider extends AbstractSliderButton {
        private final Component label; private final double min; private final double max; private final double step;
        private final DoubleConsumer setter; private final String suffix;
        private ValueSlider(int x, int y, Component label, double initial, double min, double max, double step, DoubleConsumer setter, String suffix) {
            super(x, y, 150, 20, CommonComponents.EMPTY, (initial - min) / (max - min));
            this.label = label; this.min = min; this.max = max; this.step = step; this.setter = setter; this.suffix = suffix; updateMessage();
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
