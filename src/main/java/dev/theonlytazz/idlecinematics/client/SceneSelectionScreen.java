package dev.theonlytazz.idlecinematics.client;

import dev.theonlytazz.idlecinematics.api.CinematicPreset;
import dev.theonlytazz.idlecinematics.client.shots.ShotRegistry;
import dev.theonlytazz.idlecinematics.config.ClientSettingsDraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.Locale;

/** Transactional per-preset selector. Changes remain in the parent settings draft until Apply is pressed. */
public final class SceneSelectionScreen extends Screen {
    private final Screen parent;
    private final ClientSettingsDraft draft;
    private int page;
    private int rowsPerPage;
    private int pageCount;

    public SceneSelectionScreen(Screen parent, ClientSettingsDraft draft) {
        super(Component.translatable("idlecinematics.settings.scene_selection"));
        this.parent = parent;
        this.draft = draft;
    }

    @Override protected void init() {
        List<CinematicPreset> presets = ShotRegistry.active().presets();
        rowsPerPage = Math.max(1, (height - 90) / 24);
        pageCount = Math.max(1, (presets.size() + rowsPerPage - 1) / rowsPerPage);
        page = Math.min(page, pageCount - 1);
        int first = page * rowsPerPage;
        int last = Math.min(first + rowsPerPage, presets.size());
        for (int index = first; index < last; index++) {
            CinematicPreset preset = presets.get(index);
            String id = preset.id().toString();
            addRenderableWidget(CycleButton.onOffBuilder(draft.sceneEnabled(id))
                    .create(width / 2 - 155, 42 + (index - first) * 24, 310, 20, sceneName(preset),
                            (button, enabled) -> draft.setSceneEnabled(id, enabled)));
        }

        int bottom = height - 28;
        Button previous = Button.builder(Component.translatable("idlecinematics.settings.previous"), button -> changePage(-1))
                .bounds(width / 2 - 155, bottom, 96, 20).build();
        previous.active = page > 0;
        addRenderableWidget(previous);
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(width / 2 - 49, bottom, 96, 20).build());
        Button next = Button.builder(Component.translatable("idlecinematics.settings.next"), button -> changePage(1))
                .bounds(width / 2 + 57, bottom, 96, 20).build();
        next.active = page + 1 < pageCount;
        addRenderableWidget(next);
    }

    private void changePage(int direction) {
        int selected = Math.max(0, Math.min(pageCount - 1, page + direction));
        if (selected != page) { page = selected; rebuild(); }
    }

    private void rebuild() { clearWidgets(); init(); }

    private static Component sceneName(CinematicPreset preset) {
        String[] words = preset.id().path().split("_");
        StringBuilder name = new StringBuilder();
        for (String word : words) {
            if (!name.isEmpty()) name.append(' ');
            name.append(word.substring(0, 1).toUpperCase(Locale.ROOT)).append(word.substring(1));
        }
        if (!preset.id().namespace().equals("idlecinematics")) name.append(" (").append(preset.id().namespace()).append(')');
        return Component.literal(name.toString());
    }

    @Override public void onClose() { minecraft.setScreen(parent); }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, Component.translatable("idlecinematics.settings.scene_selection_header", page + 1, pageCount),
                width / 2, 18, 0xFFFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
