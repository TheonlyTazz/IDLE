# Scene Cookbook

## Player orbit

Use pool `player`, subject `context.player()`, anchor and focus at the player's focus, and
`YawMode.FORWARD_ONLY`. Drive azimuth from elapsed seconds when you want constant angular velocity independent of
shot duration:

```java
double azimuth = startingAngle + elapsedSeconds * 9.0;
```

Prefer `TransitionSpec.continueOrbit()` when moving between compatible orbits.

## Terrain scout

Use `context.terrainTarget()` as the subject and `context.mostOpenDirection()` to orient the composition. Return
zero from `contextScore` when either is missing. Never manufacture random unchecked world coordinates.

## Foreground parallax

Require `context.parallaxDirection().isPresent()`. Place the anchor near the player, keep the validated terrain
subject as focus, and animate `lateralOffset`. Use a larger collision radius if foreground geometry is close.

## Entity portrait

Require tag `entity` and select `context.selectedSubject().orElse(context.player())`. IDLE retains entity subjects by
UUID, updates their focus during the shot, force-renders only that subject when required, and reselects if it
disappears. Do not retain a live `Entity` instance in the motion object.

## Cave composition

Use pool and tag `cave`, `SafetyPolicy.cave()`, shorter durations, and modest distance. Directional probes and wall
targets are safer framing inputs than arbitrary offsets.

## Celestial composition

Use the matching time pool, tag `open_sky`, and select `context.celestialTarget()`. Keep the day-phase check in
`contextScore` as well, so the preset remains self-describing and easy to test.

## Weather scene

Weather is not enforced by a tag. Express it through score:

```java
return context.weather() == CinematicContext.Weather.CLEAR ? 0.0 : 1.7;
```

## Multi-stage motion

Use progress ranges without mutating external state:

```java
if (progress < 0.35) {
    // opening hold or push
} else if (progress < 0.75) {
    // tracking movement
} else {
    // final reveal
}
```

Keep boundaries continuous so physical damping corrects small motion changes rather than hiding jumps.
