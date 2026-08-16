package dev.theonlytazz.idlecinematics.integration.kubejs;

import dev.theonlytazz.idlecinematics.api.CinematicLandmarkDefinition;

import java.util.ArrayList;
import java.util.List;

public final class KubeLandmarkBuilder {
    private final String id;
    private final List<String> blocks = new ArrayList<>();
    private double offsetX = 0.5;
    private double offsetY = 0.5;
    private double offsetZ = 0.5;
    private double radius = 1.5;
    private double score = 1.0;
    private double searchRadius = 48.0;
    private final List<String> tags = new ArrayList<>();

    KubeLandmarkBuilder(String id, String block) {
        this.id = id;
        blocks.add(block);
    }

    public KubeLandmarkBuilder block(String value) { blocks.add(value); return this; }
    public KubeLandmarkBuilder offset(double x, double y, double z) { offsetX = x; offsetY = y; offsetZ = z; return this; }
    public KubeLandmarkBuilder radius(double value) { radius = value; return this; }
    public KubeLandmarkBuilder score(double value) { score = value; return this; }
    public KubeLandmarkBuilder searchRadius(double value) { searchRadius = value; return this; }
    public KubeLandmarkBuilder tag(String value) { tags.add(value); return this; }

    CinematicLandmarkDefinition build() {
        CinematicLandmarkDefinition.Builder builder = CinematicLandmarkDefinition.builder(id)
                .focusOffset(offsetX, offsetY, offsetZ).radius(radius).score(score).searchRadius(searchRadius);
        for (String block : blocks) builder.block(block);
        for (String tag : tags) builder.tags(tag);
        return builder.build();
    }
}
