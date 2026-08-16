package dev.theonlytazz.idlecinematics.client;

import dev.theonlytazz.idlecinematics.config.ClientSettingsDraft;
import net.minecraft.client.gui.GuiGraphics;
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

    @Override protected void repositionElements() { rebuildWidgets(); }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        renderVanillaPanel(graphics, width, height);
        graphics.drawCenteredString(font, title, width / 2, 28, 0xFFFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    static void renderVanillaPanel(GuiGraphics graphics, int width, int height) {
        graphics.fill(0, 62, width, height - 32, 0x78000000);
        graphics.fill(0, 61, width, 63, 0x90000000);
        graphics.fill(0, height - 33, width, height - 31, 0x90000000);
    }
}
