# Versioning and Compatibility

## API version

IDLE 1.1 exposes API version 1:

```java
if (CinematicPresets.API_VERSION != 1) {
    throw new IllegalStateException("Unsupported IDLE preset API");
}
```

Prefer a compatible range check if a future add-on can support multiple revisions. API version changes are reserved
for source or behavioral contract breaks; adding context data or helper factories does not necessarily require one.

## Minecraft branches

The API packages, semantic behavior, and tests are kept in parity between Minecraft 26.1.2 and 1.21.1. Compile a
separate add-on artifact against each matching IDLE/Minecraft branch because Minecraft and NeoForge class names can
differ even when the preset source itself is shared.

Keep shared presets in a common source set and isolate only bootstrap or mapping differences in platform source
sets. Do not bundle either IDLE JAR inside the add-on.

## Client-only contract

IDLE and its preset API are client-only. Register the add-on entry point and event subscriber only on `Dist.CLIENT`.
The add-on does not need server installation unless it contains unrelated server features.

## Compatibility rules

- Never call `Options.setCameraType` or change perspective.
- Never set camera fields directly; return `CinematicRigState` values.
- Never trigger chunk loading during scoring or motion sampling.
- Never retain live entity objects; retain the supplied semantic subject.
- Never use `ThreadLocalRandom`; use the injected `RandomGenerator`.
- Avoid rendering mixins. IDLE exposes the active player/entity subjects to its narrow renderer integration.
- Treat `contextScore`, subject selection, and motion sampling as fast client-thread operations.

## Failure behavior

Invalid namespaced identifiers fail when constructed. Duplicate identifiers fail during registration. Late
registration fails after client setup. A motion returning non-finite scalar values is sanitized where possible, but
add-ons should test finite output and treat malformed vectors as programming errors.
