package dev.theonlytazz.idlecinematics.client;

import dev.theonlytazz.idlecinematics.config.ClientConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.DoubleConsumer;

public final class IdleSettingsScreen extends Screen {
    private final Screen parent;

    public IdleSettingsScreen(Screen parent) {
        super(Component.translatable("idlecinematics.settings.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = width / 2 - 155;
        int right = width / 2 + 5;
        int y = height / 2 - 92;
        addRenderableWidget(CycleButton.onOffBuilder(ClientConfig.ENABLED.getAsBoolean()).create(left, y, 150, 20,
                Component.translatable("idlecinematics.settings.enabled"), (button, value) -> ClientConfig.ENABLED.set(value)));
        addRenderableWidget(CycleButton.onOffBuilder(ClientConfig.HIDE_HUD.getAsBoolean()).create(right, y, 150, 20,
                Component.translatable("idlecinematics.settings.hide_hud"), (button, value) -> ClientConfig.HIDE_HUD.set(value)));

        y += 24;
        addRenderableWidget(new ValueSlider(left, y, Component.translatable("idlecinematics.settings.timeout"),
                ClientConfig.AFK_TIMEOUT_SECONDS.getAsInt(), 5, 300, 1, value -> ClientConfig.AFK_TIMEOUT_SECONDS.set((int) value), "s"));
        addRenderableWidget(new ValueSlider(right, y, Component.translatable("idlecinematics.settings.shot_duration"),
                ClientConfig.SHOT_DURATION_SECONDS.getAsInt(), 5, 30, 1, value -> ClientConfig.SHOT_DURATION_SECONDS.set((int) value), "s"));

        y += 24;
        addRenderableWidget(new ValueSlider(left, y, Component.translatable("idlecinematics.settings.speed"),
                ClientConfig.PAN_SPEED.getAsDouble(), 0.1, 4.0, 0.1, ClientConfig.PAN_SPEED::set, "x"));
        addRenderableWidget(new ValueSlider(right, y, Component.translatable("idlecinematics.settings.distance"),
                ClientConfig.CAMERA_DISTANCE.getAsDouble(), 0.6, 1.6, 0.1, ClientConfig.CAMERA_DISTANCE::set, "x"));

        y += 24;
        addRenderableWidget(CycleButton.<ClientConfig.ShotMode>builder(value -> Component.translatable("idlecinematics.shot_mode." + value.name().toLowerCase()), ClientConfig.SHOT_MODE.get())
                .withValues(List.of(ClientConfig.ShotMode.values()))
                .create(left, y, 150, 20, Component.translatable("idlecinematics.settings.shot_mode"),
                        (button, value) -> ClientConfig.SHOT_MODE.set(value)));
        addRenderableWidget(CycleButton.onOffBuilder(ClientConfig.INCLUDE_ENTITIES.getAsBoolean()).create(right, y, 150, 20,
                Component.translatable("idlecinematics.settings.entities"), (button, value) -> ClientConfig.INCLUDE_ENTITIES.set(value)));

        y += 24;
        addRenderableWidget(CycleButton.onOffBuilder(ClientConfig.SMOOTH_TRANSITIONS.getAsBoolean()).create(left, y, 150, 20,
                Component.translatable("idlecinematics.settings.smoothing"), (button, value) -> ClientConfig.SMOOTH_TRANSITIONS.set(value)));
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose()).bounds(right, y, 150, 20).build());
    }

    @Override
    public void onClose() {
        ClientConfig.SPEC.save();
        minecraft.setScreen(parent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.centeredText(font, title, width / 2, height / 2 - 118, 0xFFFFFFFF);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private static final class ValueSlider extends AbstractSliderButton {
        private final Component label;
        private final double min;
        private final double max;
        private final double step;
        private final DoubleConsumer setter;
        private final String suffix;

        private ValueSlider(int x, int y, Component label, double initial, double min, double max, double step,
                            DoubleConsumer setter, String suffix) {
            super(x, y, 150, 20, CommonComponents.EMPTY, (initial - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.step = step;
            this.setter = setter;
            this.suffix = suffix;
            updateMessage();
        }

        private double selected() {
            return Math.round((min + value * (max - min)) / step) * step;
        }

        @Override
        protected void updateMessage() {
            double selected = selected();
            String text = step >= 1.0 ? Integer.toString((int) selected) : String.format(java.util.Locale.ROOT, "%.1f", selected);
            setMessage(Component.empty().append(label).append(": " + text + suffix));
        }

        @Override
        protected void applyValue() {
            setter.accept(selected());
        }
    }
}
