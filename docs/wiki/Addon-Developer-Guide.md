# Add-on Developer Guide

## 1. Add IDLE as a compile dependency

Until an API artifact is published to a Maven repository, place the matching IDLE JAR in your add-on's `libs`
directory and use it as a compile-only dependency:

```groovy
dependencies {
    compileOnly files("libs/idlecinematics-neoforge-26.1.2-1.3.0.jar")
}
```

Use the `1.21.1` JAR when compiling that branch. Declare IDLE as a required client dependency in your
`neoforge.mods.toml` because the add-on links its Java API:

```toml
[[dependencies.my_idle_addon]]
modId="idlecinematics"
type="required"
versionRange="[1.3.0,2.0.0)"
ordering="AFTER"
side="CLIENT"
```

## 2. Register presets

The simplest and earliest route is a direct call from the add-on mod constructor. Construction order does not
matter: calling this method initializes IDLE's built-ins if necessary and leaves registration open.

```java
@Mod(value = MyIdleAddon.MOD_ID, dist = Dist.CLIENT)
public final class MyIdleAddon {
    public static final String MOD_ID = "my_idle_addon";

    public MyIdleAddon() {
        CinematicPresets.register(new CliffRevealPreset());
    }
}
```

For add-ons that prefer event-driven registration, listen on NeoForge's main event bus. IDLE posts this event once
during client setup and freezes the registry immediately after all handlers return:

```java
@EventBusSubscriber(modid = MyIdleAddon.MOD_ID, value = Dist.CLIENT)
public final class IdlePresetEvents {
    private IdlePresetEvents() {}

    @SubscribeEvent
    static void registerIdlePresets(RegisterCinematicPresetsEvent event) {
        event.register(new CliffRevealPreset());
    }
}
```

Do not use both routes for the same identifier. Duplicate identifiers fail with an `IllegalArgumentException`, and
registration after client setup fails with an `IllegalStateException`.

Every successfully registered preset automatically appears in IDLE's **Choose individual scenes** submenu. The
display name is derived from the identifier path, and the add-on namespace is shown in parentheses. Add-ons do not
need to register settings widgets or translations for this list.

## 3. Implement a preset

```java
public final class CliffRevealPreset implements CinematicPreset {
    private static final NamespacedId ID = new NamespacedId("my_idle_addon", "cliff_reveal");

    @Override public NamespacedId id() { return ID; }
    @Override public String pool() { return "landscape"; }
    @Override public Set<String> tags() { return Set.of("environment", "wide", "open_sky"); }

    @Override
    public double contextScore(CinematicContext context) {
        if (!context.openSky() || !context.openArea() || context.effectiveRenderDistance() < 10) return 0.0;
        return context.terrainTarget().isPresent() ? 2.0 : 0.0;
    }

    @Override
    public CinematicSubject selectSubject(CinematicContext context, RandomGenerator random) {
        return context.terrainTarget().orElse(context.player());
    }

    @Override
    public CameraMotion createMotion(CinematicContext context, CinematicSubject subject, RandomGenerator random) {
        double startAngle = random.nextDouble(360.0);
        return (progress, elapsedSeconds) -> new CinematicRigState(
                context.player().focus(),
                subject.focus(),
                7.0 + progress * 2.0,
                startAngle + progress * 25.0,
                12.0 + progress * 20.0,
                (progress - 0.5) * 2.0,
                progress * 1.5,
                0.0,
                OptionalDouble.of(58.0),
                subject,
                CinematicRigState.YawMode.SHORTEST_PATH
        );
    }

    @Override public TransitionSpec transition() { return TransitionSpec.matchMove(); }
    @Override public SafetyPolicy safety() { return SafetyPolicy.standard(); }
    @Override public DurationRange duration() { return new DurationRange(7.0, 11.0); }
}
```

## 4. Test eligibility and motion separately

All random decisions must use the supplied `RandomGenerator`. This makes scene selection and generated motion
deterministic in tests. At minimum, test that:

- `contextScore` is zero in unsafe or irrelevant contexts;
- every sampled number is finite at progress `0`, `0.5`, and `1`;
- distance stays useful inside the declared safety range;
- entity subjects gracefully fall back when absent;
- the namespaced identifier is unique.
