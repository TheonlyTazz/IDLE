# Scene Settings

Open **Settings → Scenes → Choose individual scenes...** to see every cinematic preset currently registered
with IDLE. This includes IDLE's built-in scenes and scenes supplied by installed add-ons.

Each row independently enables or disables one preset. Add-on rows include their mod namespace in parentheses so
similarly named scenes remain distinguishable. Scenes are grouped by pool in a vanilla-style scrolling list. Use
the mouse wheel or scrollbar to browse. **Reset to Default** asks for confirmation, then enables every individual
preset without changing the parent page's scene-pool choices. **Done** returns to the Scenes category.

Scene changes use the same draft as the rest of IDLE's settings:

- **Done** on the category hub saves every change.
- Escape from the category hub discards every change, including scene toggles.
- **Reset All** restores documented defaults, including enabling every registered scene, in the draft.

The four scene-pool switches remain useful as broad filters. For example, disabling **Entity scenes** skips the
entire entity pool even if individual entity presets are enabled. **Feature nearby mobs** controls whether living
entities may be selected as subjects.

Disabled identifiers are stored in the existing client configuration file under `disabledCinematicPresets`. The
older `enableNewMotionFamilies` key is retained for compatibility with configurations from earlier IDLE builds. It
affects only the five matching IDLE built-ins and never hides addon presets. The first individual change migrates
that legacy choice into independent scene selections.

IDLE may still use its tight internal player fallback when no enabled preset can produce a safe camera in the
current surroundings. This prevents an empty or obstructed selection from breaking cinematic mode.
