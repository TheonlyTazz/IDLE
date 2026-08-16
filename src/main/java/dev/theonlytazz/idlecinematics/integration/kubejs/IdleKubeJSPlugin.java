package dev.theonlytazz.idlecinematics.integration.kubejs;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;

public final class IdleKubeJSPlugin implements KubeJSPlugin {
    private static boolean registered;

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(IdleKubeEvents.GROUP);
    }

    @Override
    public void registerClasses(ClassFilter filter) {
        filter.allow("dev.theonlytazz.idlecinematics.integration.kubejs");
    }

    @Override
    public void initStartup() {
        if (registered || !IdleKubeEvents.REGISTER.hasListeners()) return;
        IdleKubeRegistrationEvent event = new IdleKubeRegistrationEvent();
        IdleKubeEvents.REGISTER.post(event);
        event.commit();
        registered = true;
    }
}
