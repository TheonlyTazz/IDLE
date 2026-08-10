package dev.theonlytazz.idlecinematics.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.theonlytazz.idlecinematics.IdleCinematics;
import dev.theonlytazz.idlecinematics.config.ClientConfig;
import dev.theonlytazz.idlecinematics.core.ActivityState;
import dev.theonlytazz.idlecinematics.platform.mixin.CameraAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = IdleCinematics.MOD_ID, value = Dist.CLIENT)
public final class ClientRuntime {
    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath(IdleCinematics.MOD_ID, "controls"));
    private static final KeyMapping TOGGLE = new KeyMapping("key.idlecinematics.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F8, CATEGORY);
    private static final KeyMapping FORCE = new KeyMapping("key.idlecinematics.force", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F9, CATEGORY);
    private static final ActivityState ACTIVITY = new ActivityState();
    private static final CinematicController CAMERA = new CinematicController();
    private static double lastMouseX = Double.NaN;
    private static double lastMouseY = Double.NaN;

    private ClientRuntime() {}

    @SubscribeEvent
    static void registerKeys(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(TOGGLE);
        event.register(FORCE);
    }

    @SubscribeEvent
    static void onTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        pollMouse(minecraft);
        while (TOGGLE.consumeClick()) {
            boolean enabled = !ClientConfig.ENABLED.getAsBoolean();
            ClientConfig.ENABLED.set(enabled);
            ClientConfig.SPEC.save();
            stop();
            minecraft.gui.setOverlayMessage(Component.translatable(enabled ? "idlecinematics.enabled" : "idlecinematics.disabled"), false);
        }
        while (FORCE.consumeClick()) {
            if (ACTIVITY.isCinematic()) stop(); else start(minecraft);
        }
        boolean usable = minecraft.player != null && minecraft.level != null && minecraft.screen == null && !minecraft.isPaused();
        if (ACTIVITY.tick(usable, ClientConfig.ENABLED.getAsBoolean(), ClientConfig.AFK_TIMEOUT_SECONDS.getAsInt() * 20)) {
            CAMERA.start(minecraft);
        }
        if (ACTIVITY.isCinematic()) CAMERA.tick(minecraft);
    }

    private static void pollMouse(Minecraft minecraft) {
        double x = minecraft.mouseHandler.xpos();
        double y = minecraft.mouseHandler.ypos();
        if (!Double.isNaN(lastMouseX) && (Math.abs(x - lastMouseX) > 0.25 || Math.abs(y - lastMouseY) > 0.25)) activity();
        lastMouseX = x;
        lastMouseY = y;
    }

    @SubscribeEvent
    static void onKey(InputEvent.Key event) {
        if (event.getAction() == InputConstants.PRESS) activity();
    }

    @SubscribeEvent
    static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (event.getAction() == InputConstants.PRESS) activity();
    }

    @SubscribeEvent
    static void onScroll(InputEvent.MouseScrollingEvent event) { activity(); }

    @SubscribeEvent
    static void hideHud(RenderGuiEvent.Pre event) {
        if (ACTIVITY.isCinematic() && ClientConfig.HIDE_HUD.getAsBoolean()) event.setCanceled(true);
    }

    private static void activity() {
        if (ACTIVITY.activity()) CAMERA.stop();
    }

    private static void start(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.level == null) return;
        ACTIVITY.forceStart();
        CAMERA.start(minecraft);
    }

    private static void stop() {
        ACTIVITY.reset();
        CAMERA.stop();
    }

    public static void applyCamera(Camera camera, float partialTick) {
        if (!ACTIVITY.isCinematic()) return;
        CameraPose pose = CAMERA.sample(partialTick);
        if (pose == null) return;
        CameraAccessor access = (CameraAccessor) camera;
        access.idlecinematics$setPosition(pose.position());
        access.idlecinematics$setRotation(pose.yaw(), pose.pitch(), pose.roll());
    }

    public static boolean isCinematicActive() {
        return ACTIVITY.isCinematic();
    }
}
