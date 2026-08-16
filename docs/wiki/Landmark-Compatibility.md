# Landmark Compatibility

IDLE 1.2 adds cinematic landmarks: stationary points of interest such as ritual altars, portals, reactors, and
multiblock controllers. A landmark definition identifies a block entity and describes its useful framing size,
focus offset, selection weight, and semantic tags. IDLE supplies the camera motion, transitions, collision checks,
and safe fallback.

Pack authors who do not want to compile an addon can use the complete [KubeJS Integration](KubeJS-Integration.md).

## Runtime behavior

- Discovery runs only when IDLE selects a new shot.
- Only block entities from already-loaded client chunks are inspected.
- The scan is bounded to 48 blocks and at most 64 results are retained.
- Missing optional mods require no compatibility classes and produce no errors or log messages.
- IDLE never requests a chunk while scanning.
- If planning cannot find a safe camera path, normal bounded reselection and the guaranteed player fallback apply.

## Included identifier compatibility

IDLE includes identifier-only definitions for prominent landmarks from NeoVitae, EvilCraft, Occultism,
Hephaestus, Nautec, Ars Nouveau/Ars Magica Legacy, Thaumaturge, Oritech, Mekanism, Immersive Engineering, Powah,
Roots Classic, Ender IO, Applied Energistics 2, and XyCraft. These are not hard dependencies or integrations with
their source code.

The NeoVitae blood altar is registered as `neovitae:ara_vitae`; `neovitea:blood_altar` is not its registry ID.

## Add a landmark

Register during the client mod constructor:

```java
CinematicLandmarks.register(CinematicLandmarkDefinition.builder("my_addon:malum_worktable")
        .block("malum:spirit_altar")
        .focusOffset(0.5, 1.0, 0.5)
        .radius(3.0)
        .score(2.5)
        .searchRadius(40.0)
        .tags("magic", "ritual")
        .build());
```

The definition automatically participates in IDLE's three generic landmark scenes. Registration can instead be
performed from `RegisterCinematicLandmarksEvent`. Identifiers must be unique and registration closes during client
setup.

An optional filter can require a formed or active machine:

```java
.filter(candidate -> candidate.blockEntity()
        .filter(MyControllerBlockEntity.class::isInstance)
        .map(MyControllerBlockEntity.class::cast)
        .map(MyControllerBlockEntity::isFormed)
        .orElse(false))
```

This kind of filter belongs in a dedicated compatibility addon that has the target mod as a compile dependency.
IDLE itself deliberately uses only registry identifiers.

## Add a specialized scene

A preset can target one registered landmark through the immutable context:

```java
private static final NamespacedId ALTAR = new NamespacedId("my_addon", "malum_worktable");

@Override
public double contextScore(CinematicContext context) {
    return context.selectedLandmark().filter(value -> value.typeId().equals(ALTAR)).isPresent() ? 4.0 : 0.0;
}

@Override
public CinematicSubject selectSubject(CinematicContext context, RandomGenerator random) {
    return context.selectedLandmark().orElseThrow().subject();
}
```

Use pool `landmark` and tag the preset with both `environment` and `landmark`. The tag prevents selection when no
validated landmark exists. Custom motion must remain semantic and must not mutate Minecraft state.
