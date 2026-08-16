package dev.theonlytazz.idlecinematics.client.landmark;

import dev.theonlytazz.idlecinematics.api.CinematicLandmark;
import dev.theonlytazz.idlecinematics.api.CinematicLandmarkDefinition;
import dev.theonlytazz.idlecinematics.api.LandmarkCandidate;
import dev.theonlytazz.idlecinematics.core.NamespacedId;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LandmarkRegistry {
    private static LandmarkRegistry active;
    private final Map<NamespacedId, CinematicLandmarkDefinition> definitions = new LinkedHashMap<>();
    private final Map<NamespacedId, List<CinematicLandmarkDefinition>> definitionsByBlock = new LinkedHashMap<>();
    private boolean frozen;

    public synchronized LandmarkRegistry register(CinematicLandmarkDefinition definition) {
        if (frozen) throw new IllegalStateException("Cinematic landmark registry is immutable");
        CinematicLandmarkDefinition previous = definitions.putIfAbsent(definition.id(), definition);
        if (previous != null) throw new IllegalArgumentException("Duplicate cinematic landmark identifier: " + definition.id());
        for (NamespacedId block : definition.blocks()) {
            definitionsByBlock.computeIfAbsent(block, ignored -> new ArrayList<>()).add(definition);
        }
        return this;
    }

    public synchronized List<CinematicLandmarkDefinition> definitions() { return List.copyOf(definitions.values()); }

    public List<CinematicLandmark> detect(LandmarkCandidate candidate, double distance) {
        List<CinematicLandmark> result = new ArrayList<>();
        List<CinematicLandmarkDefinition> matching;
        synchronized (this) {
            matching = List.copyOf(definitionsByBlock.getOrDefault(candidate.blockId(), List.of()));
        }
        for (CinematicLandmarkDefinition definition : matching) {
            if (distance <= definition.searchRadius()) definition.detect(candidate).ifPresent(result::add);
        }
        return List.copyOf(result);
    }

    public synchronized boolean recognizes(NamespacedId blockId) {
        return definitionsByBlock.containsKey(blockId);
    }

    public synchronized void freeze() { frozen = true; }
    public synchronized boolean frozen() { return frozen; }

    public static LandmarkRegistry createBuiltIns() {
        LandmarkRegistry registry = new LandmarkRegistry();
        BuiltInLandmarks.create().forEach(registry::register);
        return registry;
    }

    public static synchronized LandmarkRegistry active() {
        if (active == null) active = createBuiltIns();
        return active;
    }
}
