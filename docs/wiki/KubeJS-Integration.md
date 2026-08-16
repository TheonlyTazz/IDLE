# KubeJS Integration

IDLE 1.2 exposes cinematic landmarks and parameterized landmark scenes to KubeJS. Pack authors can add support
for a modded altar, machine, portal, or multiblock controller without compiling a Java addon.

KubeJS remains optional. If it is absent, IDLE does not load the integration classes and continues normally.

## Requirements

- IDLE 1.2.0 or newer.
- KubeJS 7.1 or newer for Minecraft 1.21.1, or KubeJS 8 for Minecraft 26.1.2.
- A block that has a client-visible block entity. Plain decorative blocks are not scanned.
- A full client restart after changing landmark startup scripts.

Definitions belong in `kubejs/startup_scripts`. They are read while IDLE's registries are still mutable. Do not put
them in `server_scripts` or `client_scripts`.

## Minimal landmark

Create `kubejs/startup_scripts/idle_landmarks.js`:

```js
IdleEvents.register(event => {
  event.landmark('my_pack:blood_altar', 'neovitae:ara_vitae')
})
```

This is enough to make the block eligible for IDLE's built-in landmark orbit, reveal, and crane scenes. Both IDs
must be namespaced and lowercase:

- `my_pack:blood_altar` is the stable landmark type owned by the pack.
- `neovitae:ara_vitae` is the target block's registry ID.

Use the pack's namespace for the first ID. Do not use `idlecinematics`, which is reserved for IDLE's definitions.

## Landmark options

Builder calls can be chained:

```js
IdleEvents.register(event => {
  event.landmark('my_pack:blood_altar', 'neovitae:ara_vitae')
    .offset(0.5, 1.0, 0.5)
    .radius(4.5)
    .score(4.0)
    .searchRadius(40.0)
    .tag('magic')
    .tag('ritual')
    .tag('multiblock')
})
```

| Method | Default | Meaning |
|---|---:|---|
| `block(id)` | — | Adds another block ID representing the same logical landmark. |
| `offset(x, y, z)` | `0.5, 0.5, 0.5` | Focus point relative to the matched block's lower corner. |
| `radius(blocks)` | `1.5` | Approximate visual radius used to choose camera distance. |
| `score(weight)` | `1.0` | Relative priority when several different landmarks are nearby. |
| `searchRadius(blocks)` | `48.0` | Per-definition range, capped by IDLE's global 48-block scan. |
| `tag(value)` | — | Adds semantic metadata for Java addons and future scene selection. |

All numeric values must be finite and positive, except individual offset coordinates, which may be negative.

### Multiple block variants

Use `block` when several registry IDs should behave as one landmark:

```js
IdleEvents.register(event => {
  event.landmark('my_pack:powah_reactor', 'powah:reactor_starter')
    .block('powah:reactor_basic')
    .block('powah:reactor_hardened')
    .block('powah:reactor_blazing')
    .block('powah:reactor_niotic')
    .block('powah:reactor_spirited')
    .block('powah:reactor_nitro')
    .radius(3.5)
    .tag('reactor')
})
```

This also helps maintain one script across Minecraft versions when a mod renamed a controller block:

```js
event.landmark('my_pack:oritech_reactor', 'oritech:nuclear_reactor_controller')
  .block('oritech:reactor_controller')
```

An ID that is not registered by the current mod set simply never matches.

## Custom landmark scenes

Pack scripts can create a scene tied to a landmark type:

```js
IdleEvents.register(event => {
  event.landmark('my_pack:blood_altar', 'neovitae:ara_vitae')
    .offset(0.5, 1.0, 0.5)
    .radius(4.5)
    .score(4.0)

  event.scene('my_pack:blood_altar_circle', 'my_pack:blood_altar', 'orbit')
    .weight(1.6)
    .distance(1.35)
    .speed(9.0)
    .elevation(16.0)
    .duration(8.0, 13.0)
    .transition('continue_orbit')
    .fov(58.0)
})
```

The first ID is the scene/preset ID. The second must be the logical landmark type ID, not the target block ID.
Scripted scenes appear in IDLE's individual scene settings alongside native and Java-addon presets.

### Scene styles

| Style | Behavior | Useful for |
|---|---|---|
| `orbit` | Continuous horizontal orbit. `speed` is degrees per second. | Altars, reactors, portals. |
| `reveal` | Approaches while changing azimuth and elevation. `speed` is total angular travel. | Large machines and ritual layouts. |
| `crane` | Rises over the landmark while rotating. | Tall structures and multiblocks. |
| `hold` | Mostly fixed diagonal composition with subtle lateral motion. | Small tables and detailed blocks. |

### Scene options

| Method | Default | Meaning |
|---|---:|---|
| `weight(value)` | `1.0` | Relative selection weight after the landmark's own score. |
| `distance(scale)` | `1.4` | Multiplier applied to the landmark's declared radius. |
| `speed(value)` | `8.0` | Orbit speed or total angular travel, depending on style. |
| `elevation(degrees)` | `18.0` | Starting vertical camera angle. |
| `duration(min, max)` | `6.0, 10.0` | Preferred shot length in seconds. User settings remain an upper bound. |
| `transition(name)` | `damped` | `cut`, `damped`, `match_move`, or `continue_orbit`. |
| `fov(degrees)` | unset | Optional request from 1 through 179 degrees, subject to IDLE's FOV policy. |

IDLE converts these declarations into immutable native presets after startup scripts finish. JavaScript is not
executed during camera ticks or rendering.

## Complete pack example

```js
IdleEvents.register(event => {
  event.landmark('evolution2:ara_vitae', 'neovitae:ara_vitae')
    .offset(0.5, 1.0, 0.5)
    .radius(4.5)
    .score(4.0)
    .searchRadius(44.0)
    .tag('magic')
    .tag('ritual')
    .tag('multiblock')

  event.scene('evolution2:ara_vitae_orbit', 'evolution2:ara_vitae', 'orbit')
    .weight(1.8)
    .distance(1.3)
    .speed(8.5)
    .elevation(17.0)
    .duration(9.0, 14.0)
    .transition('continue_orbit')

  event.scene('evolution2:ara_vitae_detail', 'evolution2:ara_vitae', 'hold')
    .weight(0.8)
    .distance(0.9)
    .speed(4.0)
    .elevation(12.0)
    .duration(6.0, 9.0)
    .transition('damped')
    .fov(54.0)
})
```

## Finding the correct block ID

Recommended methods are:

1. Enable advanced tooltips and inspect the item in JEI/EMI.
2. Use `/kubejs hand` while holding the block item, if supported by the installed KubeJS version.
3. Use `/data get block x y z` for a placed block.
4. Check the mod's generated blockstate resources or official documentation.

The placed block must actually use the specified ID. Some multiblocks replace their controller with a different
formed-state block, and some apparent blocks are entities rather than block entities.

## Choosing focus and radius

- Begin with `offset(0.5, 0.5, 0.5)` for a one-block machine.
- Raise Y for tall renderers, portals, and controllers at the bottom of a multiblock.
- Set radius to roughly half the visible structure's widest dimension.
- Prefer the actual multiblock controller over decorative casing blocks.
- Avoid registering common cable, storage, or crafting-table blocks at high scores.

Camera placement remains collision-aware. An oversized radius may push every planned camera into walls; an
undersized radius can frame only the controller instead of the full structure.

## Selection and performance

At shot-selection time IDLE:

1. Visits only client chunks already reported as loaded.
2. Examines block entities within 48 blocks.
3. Ignores block IDs that have no registered landmark definition.
4. Stops after 256 matching block entities.
5. Retains the best 64 results.
6. Runs the normal start/midpoint/endpoint collision and visibility validation.

No scan occurs every frame, and no scan requests chunk loading. Definitions for absent mods have effectively zero
world-scanning cost beyond their registry entries.

## Reloading and errors

Landmark and preset registries freeze during client startup. Consequently:

- `/reload` does not re-register IDLE landmarks.
- Reloading only server scripts is insufficient.
- Restart the client after changing `startup_scripts/idle_landmarks.js`.
- Duplicate logical landmark IDs or scene IDs are startup errors.
- Invalid namespaced IDs, styles, transitions, FOV values, or non-positive sizes produce explicit errors.

Check `logs/kubejs/startup.log` first for syntax or validation failures. IDLE's debug overlay shows the selected
landmark type after a scene has been selected.

## Current limitations

- KubeJS definitions target block entities, not arbitrary plain blocks or structures.
- KubeJS cannot currently provide Java-level formed/active controller predicates.
- Scenes use safe parameterized templates; arbitrary JavaScript camera callbacks are intentionally unsupported.
- Multiblock bounds are approximated through `offset` and `radius` unless a Java addon supplies mod-specific logic.

Use the Java landmark API when exact formed-state checks, dynamic multiblock bounds, or a completely custom motion
implementation are required.
