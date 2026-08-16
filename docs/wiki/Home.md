# IDLE Wiki

Pack developers can extend IDLE through the [Java add-on API](Addon-Developer-Guide.md) or the
[KubeJS integration](KubeJS-Integration.md).

IDLE 1.2 exposes versioned Java APIs for adding cinematic scenes and landmarks without mixins, camera mutation,
or JSON files, plus an optional KubeJS integration for modpack-defined landmarks.
An add-on supplies semantic camera compositions; IDLE remains responsible for selection, damping, interpolation,
collision, subject retention, detached-camera rendering, and immediate cleanup.

## Start here

- [Settings](Settings.md) — configuration layout, saving, cancellation, and reset behavior.
- [Add-on Developer Guide](Addon-Developer-Guide.md) — dependency setup, registration, and a complete first preset.
- [Preset API Reference](Preset-API-Reference.md) — every API type, pool, tag, transition, and safety field.
- [Scene Cookbook](Scene-Cookbook.md) — player, terrain, entity, cave, and celestial recipes.
- [Scene Settings](Scene-Settings.md) — enable built-in and addon scenes individually.
- [KubeJS Integration](KubeJS-Integration.md) — pack-defined landmarks and safe scene templates.
- [Versioning and Compatibility](Versioning-and-Compatibility.md) — API version checks and dual-version support.

## Design contract

Add-ons describe what a shot means. They never set Minecraft's camera position directly. Each sampled
`CinematicRigState` contains an anchor, focus, distance, angles, offsets, roll, optional FOV, and subject.
IDLE resolves that state into world space and applies its safety pipeline.

The current public API version is `CinematicPresets.API_VERSION == 1`.
