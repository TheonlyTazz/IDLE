package dev.theonlytazz.idlecinematics.client;

import dev.theonlytazz.idlecinematics.config.ClientSettingsDraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/** Vanilla-style category hub. The shared draft is committed only by the main Done button. */
public final class IdleSettingsScreen extends Screen implements IdleSettingsView {
    private final Screen parent;
    private final ClientSettingsDraft draft;
    private boolean applied;

    public IdleSettingsScreen(Screen parent) {
        super(Component.translatable("idlecinematics.settings.title"));
        this.parent = parent;
        this.draft = ClientSettingsDraft.snapshot();
    }

    @Override protected void init() {
        int left = width / 2 - 155;
        int right = width / 2 + 5;
        int y = 76;
        SettingsSection[] sections = SettingsSection.values();
        for (int index = 0; index < sections.length; index++) {
            SettingsSection section = sections[index];
            int x = index % 2 == 0 ? left : right;
            int rowY = y + index / 2 * 24;
            addRenderableWidget(Button.builder(section.title(), button -> openSection(section))
                    .bounds(x, rowY, 150, 20).build());
        }

        int footerY = height - 28;
        addRenderableWidget(Button.builder(Component.translatable("idlecinematics.settings.reset_all"), button -> draft.resetDefaults())
                .bounds(width / 2 - 155, footerY, 150, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> applyAndClose())
                .bounds(width / 2 + 5, footerY, 150, 20).build());
    }

    private void openSection(SettingsSection section) {
        minecraft.setScreen(new IdleSettingsSectionScreen(this, draft, section));
    }

    private void applyAndClose() {
        draft.apply();
        applied = true;
        ClientRuntime.onSettingsApplied();
        minecraft.setScreen(parent);
    }

    @Override public void onClose() {
        if (!applied) minecraft.setScreen(parent);
    }

    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        extractVanillaPanel(graphics);
        graphics.centeredText(font, title, width / 2, 28, 0xFFFFFFFF);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    static void extractVanillaPanel(GuiGraphicsExtractor graphics) {
        int height = graphics.guiHeight();
        graphics.fill(0, 62, graphics.guiWidth(), height - 32, 0x78000000);
        graphics.fill(0, 61, graphics.guiWidth(), 63, 0x90000000);
        graphics.fill(0, height - 33, graphics.guiWidth(), height - 31, 0x90000000);
    }
}
