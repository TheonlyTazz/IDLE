package dev.theonlytazz.idlecinematics.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.theonlytazz.idlecinematics.IdleCinematics;
import dev.theonlytazz.idlecinematics.client.camera.CameraPose;
import dev.theonlytazz.idlecinematics.client.profile.TemporaryClientProfile;
import dev.theonlytazz.idlecinematics.config.ClientConfig;
import dev.theonlytazz.idlecinematics.core.ActivityState;
import dev.theonlytazz.idlecinematics.platform.mixin.CameraAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.lwjgl.glfw.GLFW;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = IdleCinematics.MOD_ID, value = Dist.CLIENT)
public final class ClientRuntime {
    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(IdleCinematics.MOD_ID, "controls"));
    private static final KeyMapping TOGGLE = new KeyMapping("key.idlecinematics.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F8, CATEGORY);
    private static final KeyMapping FORCE = new KeyMapping("key.idlecinematics.force", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F9, CATEGORY);
    private static final KeyMapping SETTINGS = new KeyMapping("key.idlecinematics.settings", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F10, CATEGORY);
    private static final Identifier HUD_LAYER = Identifier.fromNamespaceAndPath(IdleCinematics.MOD_ID, "afk_status");
    private static final Set<Identifier> HIDDEN_VANILLA_LAYERS = Set.of(VanillaGuiLayers.CROSSHAIR, VanillaGuiLayers.HOTBAR,
            VanillaGuiLayers.PLAYER_HEALTH, VanillaGuiLayers.ARMOR_LEVEL, VanillaGuiLayers.FOOD_LEVEL,
            VanillaGuiLayers.VEHICLE_HEALTH, VanillaGuiLayers.AIR_LEVEL, VanillaGuiLayers.CONTEXTUAL_INFO_BAR_BACKGROUND,
            VanillaGuiLayers.EXPERIENCE_LEVEL, VanillaGuiLayers.CONTEXTUAL_INFO_BAR, VanillaGuiLayers.SELECTED_ITEM_NAME,
            VanillaGuiLayers.SPECTATOR_TOOLTIP, VanillaGuiLayers.EFFECTS, VanillaGuiLayers.BOSS_OVERLAY,
            VanillaGuiLayers.SCOREBOARD_SIDEBAR, VanillaGuiLayers.SUBTITLE_OVERLAY);
    private static final ActivityState ACTIVITY = new ActivityState();
    private static CinematicController camera;
    private static double lastMouseX = Double.NaN;
    private static double lastMouseY = Double.NaN;
    private static boolean lastWindowActive = true;

    private ClientRuntime() {}

    @SubscribeEvent static void registerKeys(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY); event.register(TOGGLE); event.register(FORCE); event.register(SETTINGS);
    }

    @SubscribeEvent static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(HUD_LAYER, ClientRuntime::renderIdleLayer);
    }

    @SubscribeEvent static void onTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        TemporaryClientProfile.repairIfNeeded(minecraft);
        pollFocusAndMouse(minecraft);
        while (TOGGLE.consumeClick()) {
            boolean enabled = !ClientConfig.ENABLED.getAsBoolean();
            ClientConfig.ENABLED.set(enabled); ClientConfig.SPEC.save();
            stop(minecraft);
            minecraft.gui.setOverlayMessage(Component.translatable(enabled ? "idlecinematics.enabled" : "idlecinematics.disabled"), false);
        }
        while (FORCE.consumeClick()) handleTransition(minecraft, ACTIVITY.forceToggle());
        while (SETTINGS.consumeClick()) {
            if (ACTIVITY.mode() == ActivityState.Mode.ACTIVE) ACTIVITY.suspend();
            minecraft.setScreen(new IdleSettingsScreen(minecraft.screen));
        }

        boolean settings = minecraft.screen instanceof IdleSettingsView;
        if (ACTIVITY.mode() == ActivityState.Mode.SUSPENDED && !settings) ACTIVITY.resume();
        if (settings && ACTIVITY.mode() == ActivityState.Mode.ACTIVE) ACTIVITY.suspend();
        boolean chat = minecraft.screen instanceof ChatScreen;
        boolean usable = minecraft.player != null && minecraft.level != null && minecraft.player.isAlive()
                && !minecraft.player.isRemoved() && ((settings && ACTIVITY.mode() == ActivityState.Mode.SUSPENDED)
                || ((minecraft.screen == null || chat) && !minecraft.isPaused()));
        int countdown = ClientConfig.COUNTDOWN_ENABLED.getAsBoolean() ? ClientConfig.COUNTDOWN_SECONDS.getAsInt() * 20 : 0;
        handleTransition(minecraft, ACTIVITY.tick(usable, ClientConfig.ENABLED.getAsBoolean(),
                ClientConfig.AFK_TIMEOUT_SECONDS.getAsInt() * 20, countdown));
        if (ACTIVITY.mode() == ActivityState.Mode.ENTERING) begin(minecraft);
        if (ACTIVITY.isRendering()) camera().tick(minecraft);
    }

    private static void pollFocusAndMouse(Minecraft minecraft) {
        boolean active = minecraft.isWindowActive();
        if (active && !lastWindowActive && ClientConfig.EXIT_ON_FOCUS_REGAIN.getAsBoolean()) activity(minecraft);
        if (active) {
            double x = minecraft.mouseHandler.xpos(); double y = minecraft.mouseHandler.ypos();
            if (!Double.isNaN(lastMouseX) && (Math.abs(x - lastMouseX) > 0.25 || Math.abs(y - lastMouseY) > 0.25)) activity(minecraft);
            lastMouseX = x; lastMouseY = y;
        } else { lastMouseX = Double.NaN; lastMouseY = Double.NaN; }
        lastWindowActive = active;
    }

    @SubscribeEvent static void onKey(InputEvent.Key event) {
        if (event.getAction() == InputConstants.PRESS && !FORCE.matches(event.getKeyEvent())
                && !TOGGLE.matches(event.getKeyEvent()) && !SETTINGS.matches(event.getKeyEvent())) activity(Minecraft.getInstance());
    }
    @SubscribeEvent static void onMouseButton(InputEvent.MouseButton.Pre event) { if (event.getAction() == InputConstants.PRESS) activity(Minecraft.getInstance()); }
    @SubscribeEvent static void onScroll(InputEvent.MouseScrollingEvent event) { activity(Minecraft.getInstance()); }

    @SubscribeEvent static void hideVanillaLayer(RenderGuiLayerEvent.Pre event) {
        if (ACTIVITY.isRendering() && ClientConfig.HIDE_HUD.getAsBoolean() && HIDDEN_VANILLA_LAYERS.contains(event.getName())) event.setCanceled(true);
    }

    @SubscribeEvent static void cinematicFov(ViewportEvent.ComputeFov event) {
        if (ACTIVITY.isRendering()) camera().requestedFov().ifPresent(value -> event.setFOV((float) value));
    }

    private static void renderIdleLayer(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (ACTIVITY.mode() == ActivityState.Mode.COUNTDOWN) {
            int seconds = Math.max(1, (ACTIVITY.countdownTicks() + 19) / 20);
            float scale = ClientConfig.HUD_SCALE.get().floatValue();
            graphics.pose().pushMatrix();
            graphics.pose().scale(scale, scale);
            graphics.centeredText(minecraft.font, Component.translatable("idlecinematics.countdown", seconds),
                    (int) (graphics.guiWidth() / scale) / 2, (int) (graphics.guiHeight() / scale) / 2 - 10, 0xFFFFFFFF);
            graphics.pose().popMatrix();
            return;
        }
        if (!ACTIVITY.isRendering()) return;
        String text = "";
        if (ClientConfig.SHOW_AFK_TIMER.getAsBoolean()) {
            long seconds = ACTIVITY.activeTicks() / 20;
            text = String.format(java.util.Locale.ROOT, "%s%02d:%02d", ClientConfig.SHOW_TIMER_TITLE.getAsBoolean() ? "AFK · " : "", seconds / 60, seconds % 60);
        }
        if (ClientConfig.SHOW_DEBUG_PRESET.getAsBoolean()) text = text.isEmpty() ? camera().debugDescription() : text + "\n" + camera().debugDescription();
        if (!text.isEmpty()) drawAnchored(graphics, minecraft, text);
    }

    private static void drawAnchored(GuiGraphicsExtractor graphics, Minecraft minecraft, String text) {
        float scale = ClientConfig.HUD_SCALE.get().floatValue();
        graphics.pose().pushMatrix(); graphics.pose().scale(scale, scale);
        int width = (int) (graphics.guiWidth() / scale); int height = (int) (graphics.guiHeight() / scale);
        String[] lines = text.split("\\n");
        int textWidth = java.util.Arrays.stream(lines).mapToInt(minecraft.font::width).max().orElse(0); int x; int y;
        switch (ClientConfig.HUD_ANCHOR.get()) {
            case CENTER -> { x = (width - textWidth) / 2; y = height / 2 + 12; }
            case TOP_LEFT -> { x = 8; y = 8; }
            case TOP_RIGHT -> { x = width - textWidth - 8; y = 8; }
            case BOTTOM_LEFT -> { x = 8; y = height - 18; }
            case BOTTOM_RIGHT -> { x = width - textWidth - 8; y = height - 18; }
            default -> throw new IllegalStateException("Unknown HUD anchor");
        }
        for (int index = 0; index < lines.length; index++) graphics.text(minecraft.font, Component.literal(lines[index]), x, y + index * 10, 0xFFFFFFFF, true);
        graphics.pose().popMatrix();
    }

    private static void activity(Minecraft minecraft) {
        if (minecraft.screen instanceof IdleSettingsView && ACTIVITY.mode() == ActivityState.Mode.SUSPENDED) return;
        handleTransition(minecraft, ACTIVITY.activity());
    }
    private static void handleTransition(Minecraft minecraft, ActivityState.Transition transition) {
        if (transition == ActivityState.Transition.ENTER) begin(minecraft);
        else if (transition == ActivityState.Transition.EXIT) cleanup(minecraft);
    }
    private static void begin(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null) { stop(minecraft); return; }
        if (minecraft.screen instanceof ChatScreen) minecraft.setScreen(null);
        camera().start(minecraft); TemporaryClientProfile.start(minecraft); ACTIVITY.entered();
    }
    private static void stop(Minecraft minecraft) { ACTIVITY.reset(); cleanup(minecraft); }
    private static void cleanup(Minecraft minecraft) { if (camera != null) camera.stop(); TemporaryClientProfile.stop(minecraft); ACTIVITY.exited(); }

    public static void onSettingsApplied() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!ClientConfig.ENABLED.getAsBoolean()) stop(minecraft); else TemporaryClientProfile.sync(minecraft);
    }
    public static void applyCamera(Camera camera, float partialTick) {
        if (!ACTIVITY.isRendering()) return;
        CameraPose pose = camera().sample(partialTick); if (pose == null) return;
        CameraAccessor access = (CameraAccessor) camera;
        access.idlecinematics$setPosition(pose.position()); access.idlecinematics$setRotation(pose.yaw(), pose.pitch(), pose.roll());
        access.idlecinematics$setDetached(true);
    }
    public static boolean isCinematicActive() { return ACTIVITY.isRendering(); }
    public static boolean shouldForceRenderPlayer() { return ACTIVITY.isRendering() && camera().requiresPlayer(); }
    public static Optional<UUID> entitySubjectId() { return ACTIVITY.isRendering() ? camera().entitySubjectId() : Optional.empty(); }
    private static CinematicController camera() { if (camera == null) camera = new CinematicController(); return camera; }
}
