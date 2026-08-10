package dev.theonlytazz.idlecinematics.platform;

import net.minecraft.client.multiplayer.ClientLevel;

public final class ClientWorldAdapter {
    private ClientWorldAdapter() {}

    public static long dayTime(ClientLevel level) {
        return level.getDayTime();
    }
}
