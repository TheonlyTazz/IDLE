package dev.theonlytazz.idlecinematics.integration.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface IdleKubeEvents {
    EventGroup GROUP = EventGroup.of("IdleEvents");
    EventHandler REGISTER = GROUP.startup("register", () -> IdleKubeRegistrationEvent.class);
}
