# Idle Cinematics

A client-side cinematic camera for Minecraft 26.1.2 on NeoForge. After 25 seconds without keyboard or
mouse input, the camera begins collision-aware, context-aware shots. Any input exits at once.

Version 1.3 adds a public typed preset API, semantic camera compositions, collision-volume shot planning,
an explicit countdown/AFK lifecycle, transactional multi-page settings, dedicated HUD layers, and optional
crash-safe FPS, FOV, and audio profiles.

Inspired by [AFK Cinematics](https://www.curseforge.com/minecraft/mc-mods/afk-cinematics) for Fabric. Idle Cinematics
is a fresh implementation for NeoForge.

## Controls

- `F8`: enable or disable automatic activation
- `F9`: start or stop cinematic mode immediately
- `F10`: open settings (an active cinematic is suspended and resumes when the screen closes)

Client configuration is written by NeoForge to `config/idlecinematics-client.toml`. It includes the AFK timeout,
pan speed, scene pools, transition intensity, overlays, and conservative temporary profiles.

## Development

Minecraft 26.1.2 requires Java 25. Run `./gradlew build` (or `gradlew.bat build` on Windows). The distributable JAR
is produced under `build/libs`. The version-neutral state and easing code is isolated in `core` to support a later
Java 21 / Minecraft 1.21.1 adapter.

## CurseForge publishing

Create the CurseForge project, copy `.env.example` to `.env`, and set the upload token, project ID, and project slug.
The `.env` file is ignored. Each release also requires a matching file under `metadata/changelogs`.

```powershell
.\gradlew.bat publishCurseforge
```

