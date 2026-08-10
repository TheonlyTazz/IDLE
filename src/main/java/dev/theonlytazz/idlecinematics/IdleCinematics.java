package dev.theonlytazz.idlecinematics;

import dev.theonlytazz.idlecinematics.config.ClientConfig;
import dev.theonlytazz.idlecinematics.client.IdleSettingsScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = IdleCinematics.MOD_ID, dist = Dist.CLIENT)
public final class IdleCinematics {
    public static final String MOD_ID = "idlecinematics";

    public IdleCinematics(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        IConfigScreenFactory configScreenFactory = (modContainer, parent) -> new IdleSettingsScreen(parent);
        container.registerExtensionPoint(IConfigScreenFactory.class, configScreenFactory);
    }
}
