package dev.theonlytazz.idlecinematics.client;

import dev.theonlytazz.idlecinematics.api.CinematicPreset;
import dev.theonlytazz.idlecinematics.client.shots.ShotRegistry;
import dev.theonlytazz.idlecinematics.config.ClientSettingsDraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Scrollable, categorized per-preset selector modeled after vanilla's key-bind list. */
public final class SceneSelectionScreen extends Screen implements IdleSettingsView {
    private final Screen parent;
    private final ClientSettingsDraft draft;
    private SceneList sceneList;
    private Button resetButton;
    private Button doneButton;
    private EditBox searchBox;
    private String searchQuery = "";
    private final List<PresetEntry> presetEntries = new ArrayList<>();
    private final List<CategoryToggleEntry> categoryToggles = new ArrayList<>();

    public SceneSelectionScreen(Screen parent, ClientSettingsDraft draft) {
        super(Component.translatable("idlecinematics.settings.scene_selection"));
        this.parent = parent;
        this.draft = draft;
    }

    @Override protected void init() {
        migrateLegacyPoolChoices();
        presetEntries.clear();
        categoryToggles.clear();
        int searchWidth = Math.min(340, width - 32);
        searchBox = new EditBox(font, width / 2 - searchWidth / 2, 38, searchWidth, 20,
                Component.translatable("idlecinematics.settings.scene_search"));
        searchBox.setHint(Component.translatable("idlecinematics.settings.scene_search"));
        searchBox.setValue(searchQuery);
        searchBox.setResponder(value -> {
            searchQuery = value;
            if (sceneList != null) sceneList.applyFilter(value);
        });
        addRenderableWidget(searchBox);
        sceneList = addRenderableWidget(new SceneList(minecraft, width, height - 94, 62));
        int footerY = height - 28;
        resetButton = addRenderableWidget(Button.builder(Component.translatable("idlecinematics.settings.reset_to_default"),
                button -> confirmResetScenes()).bounds(width / 2 - 155, footerY, 150, 20).build());
        doneButton = addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(width / 2 + 5, footerY, 150, 20).build());
    }

    @Override protected void repositionElements() {
        if (sceneList != null) sceneList.updateSizeAndPosition(width, height - 94, 62);
        if (searchBox != null) {
            int searchWidth = Math.min(340, width - 32);
            searchBox.setWidth(searchWidth);
            searchBox.setPosition(width / 2 - searchWidth / 2, 38);
        }
        if (resetButton != null) resetButton.setPosition(width / 2 - 155, height - 28);
        if (doneButton != null) doneButton.setPosition(width / 2 + 5, height - 28);
    }

    @Override public void onClose() { minecraft.setScreen(parent); }

    private void migrateLegacyPoolChoices() {
        for (CinematicPreset preset : ShotRegistry.active().presets()) {
            if (!draft.legacyPoolEnabled(preset.pool())) draft.setSceneEnabled(preset.id().toString(), false);
        }
        draft.finishLegacyPoolMigration();
    }

    private void syncToggles() {
        presetEntries.forEach(PresetEntry::syncFromDraft);
        categoryToggles.forEach(CategoryToggleEntry::syncFromDraft);
    }

    private void confirmResetScenes() {
        minecraft.setScreen(new IdleResetConfirmScreen(confirmed -> {
            if (confirmed) draft.resetPresetDefaults();
            minecraft.setScreen(this);
        }, Component.translatable("idlecinematics.settings.reset_section_confirm_title", title),
                Component.translatable("idlecinematics.settings.reset_section_confirm_message")));
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.centeredText(font, title, width / 2, 16, 0xFFFFFFFF);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private final class SceneList extends ContainerObjectSelectionList<SceneEntry> {
        private SceneList(Minecraft minecraft, int width, int height, int y) {
            super(minecraft, width, height, y, 24);
            applyFilter(searchQuery);
        }

        private void applyFilter(String query) {
            clearEntries();
            presetEntries.clear();
            categoryToggles.clear();
            Map<String, List<CinematicPreset>> groups = new LinkedHashMap<>();
            for (CinematicPreset preset : ShotRegistry.active().presets()) {
                groups.computeIfAbsent(preset.pool(), ignored -> new ArrayList<>()).add(preset);
            }
            for (Map.Entry<String, List<CinematicPreset>> group : groups.entrySet()) {
                List<CinematicPreset> visible = group.getValue().stream()
                        .filter(preset -> matchesSearch(preset, group.getKey(), query)).toList();
                if (visible.isEmpty()) continue;
                addEntry(new CategoryEntry(group.getKey()));
                CategoryToggleEntry toggle = new CategoryToggleEntry(group.getValue());
                categoryToggles.add(toggle);
                addEntry(toggle);
                for (CinematicPreset preset : visible) {
                    PresetEntry entry = new PresetEntry(preset);
                    presetEntries.add(entry);
                    addEntry(entry);
                }
            }
        }

        @Override public int getRowWidth() { return Math.min(340, width - 32); }
    }

    private abstract static class SceneEntry extends ContainerObjectSelectionList.Entry<SceneEntry> {
    }

    private final class CategoryEntry extends SceneEntry {
        private final Component name;

        private CategoryEntry(String pool) {
            name = Component.translatable("idlecinematics.settings.scene_group", titleCase(pool));
        }

        @Override public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
            graphics.centeredText(font, name, getContentXMiddle(), getContentYMiddle() - 4, 0xFFFFFFFF);
        }

        @Override public List<? extends GuiEventListener> children() { return List.of(); }
        @Override public List<? extends NarratableEntry> narratables() { return List.of(); }
    }

    private final class CategoryToggleEntry extends SceneEntry {
        private final Component name = Component.translatable("idlecinematics.settings.scene_group_all");
        private final List<String> presetIds;
        private final CycleButton<Boolean> toggle;

        private CategoryToggleEntry(List<CinematicPreset> presets) {
            presetIds = presets.stream().map(preset -> preset.id().toString()).toList();
            toggle = CycleButton.onOffBuilder(allEnabled()).displayOnlyValue().create(0, 0, 100, 20, name,
                    (button, enabled) -> {
                        presetIds.forEach(id -> draft.setSceneEnabled(id, enabled));
                        syncToggles();
                    });
        }

        private boolean allEnabled() { return presetIds.stream().allMatch(draft::sceneEnabled); }
        private void syncFromDraft() { toggle.setValue(allEnabled()); }

        @Override public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
            toggle.setPosition(getContentRight() - toggle.getWidth(), getContentY() - 2);
            toggle.extractRenderState(graphics, mouseX, mouseY, partialTick);
            graphics.text(font, name, getContentX(), getContentYMiddle() - 4, 0xFFFFFFFF);
        }

        @Override public List<? extends GuiEventListener> children() { return List.of(toggle); }
        @Override public List<? extends NarratableEntry> narratables() { return List.of(toggle); }
    }

    private final class PresetEntry extends SceneEntry {
        private final Component name;
        private final String id;
        private final CycleButton<Boolean> toggle;

        private PresetEntry(CinematicPreset preset) {
            name = sceneName(preset);
            id = preset.id().toString();
            toggle = CycleButton.onOffBuilder(draft.sceneEnabled(id)).displayOnlyValue()
                    .create(0, 0, 100, 20, name, (button, enabled) -> {
                        draft.setSceneEnabled(id, enabled);
                        syncToggles();
                    });
        }

        private void syncFromDraft() { toggle.setValue(draft.sceneEnabled(id)); }

        @Override public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
            toggle.setPosition(getContentRight() - toggle.getWidth(), getContentY() - 2);
            toggle.extractRenderState(graphics, mouseX, mouseY, partialTick);
            graphics.text(font, name, getContentX(), getContentYMiddle() - 4, 0xFFFFFFFF);
        }

        @Override public List<? extends GuiEventListener> children() { return List.of(toggle); }
        @Override public List<? extends NarratableEntry> narratables() { return List.of(toggle); }
    }

    private static Component sceneName(CinematicPreset preset) {
        String name = titleCase(preset.id().path());
        if (!preset.id().namespace().equals("idlecinematics")) name += " (" + preset.id().namespace() + ')';
        return Component.literal(name);
    }

    private static boolean matchesSearch(CinematicPreset preset, String pool, String query) {
        String normalized = query == null ? "" : query.strip().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) return true;
        return preset.id().toString().toLowerCase(Locale.ROOT).contains(normalized)
                || sceneName(preset).getString().toLowerCase(Locale.ROOT).contains(normalized)
                || titleCase(pool).toLowerCase(Locale.ROOT).contains(normalized);
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
