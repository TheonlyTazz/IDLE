# Scene Settings

Open **Settings → Scenes → Choose individual scenes...** to see every cinematic preset currently registered
with IDLE. This includes IDLE's built-in scenes and scenes supplied by installed add-ons.

Each row independently enables or disables one preset. Add-on rows include their mod namespace in parentheses so
similarly named scenes remain distinguishable. The list is paginated to fit Minecraft's GUI scale; use **Prev** and
**Next** to move between pages and **Done** to return to the main settings screen.

Scene changes use the same draft as the rest of IDLE's settings:

- **Apply** on the main screen saves every change.
- **Cancel** or Escape on the main screen discards every change, including scene toggles.
- **Reset** enables every registered scene in the draft.

The four scene-pool switches remain useful as broad filters. For example, disabling **Entity scenes** skips the
entire entity pool even if individual entity presets are enabled. **Feature nearby mobs** controls whether living
entities may be selected as subjects.

Disabled identifiers are stored in the existing client configuration file under `disabledCinematicPresets`. The
older `enableNewMotionFamilies` key is retained for compatibility with configurations from earlier IDLE builds. It
affects only the five matching IDLE built-ins and never hides addon presets. The first individual change migrates
that legacy choice into independent scene selections.

IDLE may still use its tight internal player fallback when no enabled preset can produce a safe camera in the
current surroundings. This prevents an empty or obstructed selection from breaking cinematic mode.
