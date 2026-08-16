# IDLE Wiki

IDLE 1.3 exposes a versioned Java API for adding cinematic scenes without mixins, camera mutation, or JSON files.
An add-on supplies semantic camera compositions; IDLE remains responsible for selection, damping, interpolation,
collision, subject retention, detached-camera rendering, and immediate cleanup.

## Start here

- [Settings](Settings.md) — configuration layout, saving, cancellation, and reset behavior.
- [Add-on Developer Guide](Addon-Developer-Guide.md) — dependency setup, registration, and a complete first preset.
- [Preset API Reference](Preset-API-Reference.md) — every API type, pool, tag, transition, and safety field.
- [Scene Cookbook](Scene-Cookbook.md) — player, terrain, entity, cave, and celestial recipes.
- [Scene Settings](Scene-Settings.md) — enable built-in and addon scenes individually.
- [Versioning and Compatibility](Versioning-and-Compatibility.md) — API version checks and dual-version support.

## Design contract

Add-ons describe what a shot means. They never set Minecraft's camera position directly. Each sampled
`CinematicRigState` contains an anchor, focus, distance, angles, offsets, roll, optional FOV, and subject.
IDLE resolves that state into world space and applies its safety pipeline.

The current public API version is `CinematicPresets.API_VERSION == 1`.
