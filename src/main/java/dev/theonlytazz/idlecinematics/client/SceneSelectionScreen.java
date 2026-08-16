package dev.theonlytazz.idlecinematics.client;

import dev.theonlytazz.idlecinematics.api.CinematicPreset;
import dev.theonlytazz.idlecinematics.client.shots.ShotRegistry;
import dev.theonlytazz.idlecinematics.config.ClientSettingsDraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.Locale;

/** Scrollable, categorized per-preset selector modeled after vanilla's key-bind list. */
public final class SceneSelectionScreen extends Screen implements IdleSettingsView {
    private final Screen parent;
    private final ClientSettingsDraft draft;
    private SceneList sceneList;
    private Button resetButton;
    private Button doneButton;

    public SceneSelectionScreen(Screen parent, ClientSettingsDraft draft) {
        super(Component.translatable("idlecinematics.settings.scene_selection"));
        this.parent = parent;
        this.draft = draft;
    }

    @Override protected void init() {
        sceneList = addRenderableWidget(new SceneList(minecraft, width, height - 94, 62));
        int footerY = height - 28;
        resetButton = addRenderableWidget(Button.builder(Component.translatable("idlecinematics.settings.reset_scenes"), button -> {
            draft.resetPresetDefaults();
            rebuildWidgets();
        }).bounds(width / 2 - 155, footerY, 150, 20).build());
        doneButton = addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(width / 2 + 5, footerY, 150, 20).build());
    }

    @Override protected void repositionElements() {
        if (sceneList != null) sceneList.updateSizeAndPosition(width, height - 94, 62);
        if (resetButton != null) resetButton.setPosition(width / 2 - 155, height - 28);
        if (doneButton != null) doneButton.setPosition(width / 2 + 5, height - 28);
    }

    @Override public void onClose() { minecraft.setScreen(parent); }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 28, 0xFFFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private final class SceneList extends ContainerObjectSelectionList<SceneEntry> {
        private SceneList(Minecraft minecraft, int width, int height, int y) {
            super(minecraft, width, height, y, 24);
            String previousPool = null;
            for (CinematicPreset preset : ShotRegistry.active().presets()) {
                if (!preset.pool().equals(previousPool)) {
                    previousPool = preset.pool();
                    addEntry(new CategoryEntry(Component.translatable("idlecinematics.settings.scene_group", titleCase(previousPool))));
                }
                addEntry(new PresetEntry(preset));
            }
        }

        @Override public int getRowWidth() { return Math.min(340, width - 32); }
    }

    private abstract static class SceneEntry extends ContainerObjectSelectionList.Entry<SceneEntry> {
    }

    private final class CategoryEntry extends SceneEntry {
        private final Component name;

        private CategoryEntry(Component name) { this.name = name; }

        @Override public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                                     int mouseX, int mouseY, boolean hovering, float partialTick) {
            graphics.drawCenteredString(font, name, left + width / 2, top + height / 2 - 4, 0xFFFFFFFF);
        }

        @Override public List<? extends GuiEventListener> children() { return List.of(); }
        @Override public List<? extends NarratableEntry> narratables() { return List.of(); }
    }

    private final class PresetEntry extends SceneEntry {
        private final Component name;
        private final CycleButton<Boolean> toggle;

        private PresetEntry(CinematicPreset preset) {
            name = sceneName(preset);
            String id = preset.id().toString();
            toggle = CycleButton.onOffBuilder(draft.sceneEnabled(id)).displayOnlyValue()
                    .create(0, 0, 100, 20, name, (button, enabled) -> draft.setSceneEnabled(id, enabled));
        }

        @Override public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                                     int mouseX, int mouseY, boolean hovering, float partialTick) {
            toggle.setPosition(left + width - toggle.getWidth(), top - 2);
            toggle.render(graphics, mouseX, mouseY, partialTick);
            graphics.drawString(font, name, left, top + height / 2 - 4, 0xFFFFFFFF);
        }

        @Override public List<? extends GuiEventListener> children() { return List.of(toggle); }
        @Override public List<? extends NarratableEntry> narratables() { return List.of(toggle); }
    }

    private static Component sceneName(CinematicPreset preset) {
        String name = titleCase(preset.id().path());
        if (!preset.id().namespace().equals("idlecinematics")) name += " (" + preset.id().namespace() + ')';
        return Component.literal(name);
    }

    private static String titleCase(String value) {
        StringBuilder result = new StringBuilder();
        for (String word : value.split("_")) {
            if (!result.isEmpty()) result.append(' ');
            if (!word.isEmpty()) result.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
        }
        return result.toString();
    }
}
