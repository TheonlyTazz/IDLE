# Preset API Reference

## Registration lifecycle

`CinematicPresets.register(preset)` is open during mod construction. During client setup IDLE posts one
`RegisterCinematicPresetsEvent`, accepts registrations from its handlers, then freezes the registry. The registry is
immutable while worlds are running. `CinematicPresets.isRegistrationOpen()` is available for diagnostics, not for
late conditional registration.

## `CinematicPreset`

| Method | Contract |
|---|---|
| `id()` | A lowercase namespaced identifier owned by the add-on, such as `my_addon:cliff_reveal`. |
| `pool()` | Selection family. Use one of the supported pool names below. |
| `tags()` | Semantic and hard eligibility markers. |
| `contextScore(context)` | Return `0` to reject the scene or a positive relative selection weight. |
| `selectSubject(context, random)` | Select a stable subject from the immutable scene snapshot. |
| `createMotion(context, subject, random)` | Construct the shot's semantic motion. Capture random seeds here. |
| `transition()` | Select cut, damping, match move, or orbit continuation. |
| `safety()` | Declare distance, angle, collision, fluid, and obstruction bounds. |
| `duration()` | Preferred minimum and maximum seconds; the user's configured shot duration remains an upper bound. |

## Supported pools

| Pool | When IDLE requests it |
|---|---|
| `player` | Player-focused and dynamically selected actor scenes. |
| `landscape` | Overworld environment scenes. |
| `landmark` | A registered nearby block-entity landmark is available. |
| `entity` | A validated living-entity subject is available. |
| `cave` | The player is classified as enclosed. |
| `nether` | The current dimension is the Nether. |
| `end` | The current dimension is the End. |
| `sunrise`, `day`, `sunset`, `night` | Matching open-sky celestial periods. |

Unknown pools are legal identifiers but are only considered during the director's global fallback. Add-ons should
therefore use an existing pool in API version 1.

## Recognized eligibility tags

- `cave`: requires an enclosed scene; presets without it are excluded while enclosed.
- `open_sky`: requires direct sky visibility.
- `wide`: requires an open area and at least eight chunks of effective render distance.
- `entity`: requires a validated selected entity.
- `landmark`: requires a selected registered landmark.
- `nether`: requires the Nether.
- `end`: requires the End.

Other tags such as `player`, `environment`, `close`, `weather`, and add-on-specific strings are useful metadata.
Custom conditions belong in `contextScore`.

## `CinematicContext`

The immutable selection-time snapshot supplies:

- player, selected entity, terrain, and celestial subjects;
- dimension and day phase;
- enclosure and open-sky classification;
- clear, rain, or thunder weather;
- dry, water, lava, or other fluid state;
- local light and effective render distance;
- ceiling clearance and floor drop;
- eight directional probes;
- up to 64 nearby subjects.
- up to 64 nearby registered landmarks and the highest-ranked selected landmark.

`CinematicLandmark` supplies the definition identifier, matched block identifier, stable block position, focus,
approximate framing radius, score, and semantic tags. `subject()` converts it to a world-position subject for use
by a preset.

Each directional probe includes its normalized direction, open distance, optional foreground and wall targets,
floor drop, and camera clearance. Analysis never intentionally loads chunks.

## `CinematicRigState`

| Field | Meaning |
|---|---|
| `anchor` | World-space origin around which semantic camera distance and angles are resolved. |
| `focus` | World-space point the camera looks toward. |
| `distance` | Radial distance in blocks, before the user's distance multiplier. |
| `azimuth` | Horizontal angle in degrees. |
| `elevation` | Vertical radial angle in degrees. |
| `lateralOffset` | Tangential offset in blocks. |
| `verticalOffset` | Additional world-up offset in blocks. |
| `roll` | Camera roll in degrees. |
| `cinematicFov` | Optional raw FOV request; `OptionalDouble.empty()` uses IDLE/user policy. |
| `subject` | Subject required by rendering and fallback logic. |
| `yawMode` | Shortest-path rotation or forward-only orbit continuation. |

`CameraMotion.sample(progress, elapsedSeconds)` receives progress clamped conceptually to `0..1` and elapsed shot
time in seconds. It must be pure with respect to Minecraft: never change options, screens, entities, or the camera.

## Transitions

- `TransitionSpec.cut()` snaps on the next rendered frame.
- `TransitionSpec.damped(duration)` converges using IDLE's critically damped state.
- `TransitionSpec.matchMove()` aligns outgoing focus and azimuth before entering the new composition.
- `TransitionSpec.continueOrbit()` preserves forward azimuth continuity.

The user's transition intensity scales transitions without changing motion progress or shot speed.

## Safety

`SafetyPolicy.standard()` is appropriate for most outdoor shots; `SafetyPolicy.cave()` uses tighter bounds.
Custom policies declare minimum and maximum distance, pitch limits, collision radius, fluid policy, and obstruction
tolerance. IDLE constrains the semantic distance/elevation, checks the start/mid/end plan samples, casts a center
ray plus four volume-offset rays, checks focus visibility separately, and can reject the shot before it begins.
