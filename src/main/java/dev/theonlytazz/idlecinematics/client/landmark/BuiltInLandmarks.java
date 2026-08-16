package dev.theonlytazz.idlecinematics.client.landmark;

import dev.theonlytazz.idlecinematics.api.CinematicLandmarkDefinition;

import java.util.List;

/** Optional identifier-only compatibility. No classes or assets from the target mods are linked. */
final class BuiltInLandmarks {
    private BuiltInLandmarks() {}

    static List<CinematicLandmarkDefinition> create() {
        return List.of(
                landmark("neovitae_ara_vitae", 4.5, 4.0, "magic", "multiblock", "ritual")
                        .block("neovitae:ara_vitae").focusOffset(0.5, 1.0, 0.5).build(),
                landmark("evilcraft_spirit_portal", 3.5, 3.2, "magic", "portal")
                        .block("evilcraft:spirit_portal").focusOffset(0.5, 1.5, 0.5).build(),
                landmark("occultism_stable_wormhole", 2.5, 3.0, "magic", "portal")
                        .blocks("occultism:stable_wormhole", "occultism:stable_wormhole_dark")
                        .focusOffset(0.5, 1.0, 0.5).build(),
                landmark("hephaestus_smeltery", 5.0, 3.0, "technology", "multiblock")
                        .block("hephaestus:smeltery_controller").focusOffset(0.5, 1.5, 0.5).build(),
                landmark("nautec_bio_reactor", 5.0, 3.6, "technology", "multiblock", "underwater")
                        .block("nautec:bio_reactor").focusOffset(0.5, 1.5, 0.5).build(),
                landmark("nautec_pressure_forge", 2.5, 2.4, "technology")
                        .block("nautec:pressure_forge").focusOffset(0.5, 1.0, 0.5).build(),
                landmark("ars_altar", 4.5, 3.2, "magic", "multiblock", "ritual")
                        .blocks("arsmagicalegacy:altar_core", "ars_nouveau:enchanting_apparatus")
                        .focusOffset(0.5, 1.0, 0.5).build(),
                landmark("ars_obelisk", 2.5, 2.5, "magic", "vertical")
                        .blocks("arsmagicalegacy:obelisk", "ars_nouveau:ritual_brazier").focusOffset(0.5, 1.2, 0.5).build(),
                landmark("thaumaturge_eldritch_obelisk", 4.0, 3.5, "magic", "vertical")
                        .block("thaumaturge:eldritch_obelisk").focusOffset(0.5, 2.0, 0.5).build(),
                landmark("thaumaturge_eldritch_altar", 3.0, 3.0, "magic", "ritual")
                        .block("thaumaturge:eldritch_altar").focusOffset(0.5, 1.0, 0.5).build(),
                landmark("thaumaturge_infernal_furnace", 4.0, 2.8, "magic", "multiblock", "technology")
                        .block("thaumaturge:infernal_furnace").focusOffset(0.5, 1.5, 0.5).build(),
                landmark("oritech_atomic_forge", 3.0, 2.8, "technology")
                        .blocks("oritech:atomic_forge", "oritech:atomic_forge_block").focusOffset(0.5, 1.0, 0.5).build(),
                landmark("oritech_reactor", 6.0, 3.4, "technology", "multiblock", "reactor")
                        .blocks("oritech:nuclear_reactor_controller", "oritech:reactor_controller")
                        .focusOffset(0.5, 2.0, 0.5).build(),
                landmark("mekanism_fusion_reactor", 7.0, 3.5, "technology", "multiblock", "reactor")
                        .block("mekanismgenerators:fusion_reactor_controller").focusOffset(0.5, 2.5, 0.5).build(),
                landmark("immersiveengineering_excavator", 8.0, 3.5, "technology", "multiblock")
                        .block("immersiveengineering:excavator").focusOffset(0.5, 3.0, 0.5).build(),
                landmark("immersiveengineering_arc_furnace", 6.0, 3.2, "technology", "multiblock")
                        .block("immersiveengineering:arc_furnace").focusOffset(0.5, 2.0, 0.5).build(),
                landmark("powah_reactor", 3.5, 2.5, "technology", "reactor")
                        .blocks("powah:reactor_starter", "powah:reactor_basic", "powah:reactor_hardened",
                                "powah:reactor_blazing", "powah:reactor_niotic", "powah:reactor_spirited", "powah:reactor_nitro")
                        .focusOffset(0.5, 1.0, 0.5).build(),
                landmark("roots_altar", 2.5, 2.4, "magic", "ritual")
                        .block("rootsclassic:altar").focusOffset(0.5, 0.9, 0.5).build(),
                landmark("enderio_obelisk", 2.5, 2.2, "technology", "vertical")
                        .blocks("enderio:attractor_obelisk", "enderio:aversion_obelisk", "enderio:inhibitor_obelisk",
                                "enderio:relocator_obelisk", "enderio:weather_obelisk", "enderio:xp_obelisk")
                        .focusOffset(0.5, 1.2, 0.5).build(),
                landmark("ae2_controller", 3.5, 2.0, "technology")
                        .blocks("ae2:controller", "ae2:crystal_resonance_generator").focusOffset(0.5, 0.8, 0.5).build(),
                landmark("xycraft_power_core", 4.0, 2.4, "technology", "vertical")
                        .blocks("xycraft_machines:power_core", "xycraft_machines:hover_pylon")
                        .focusOffset(0.5, 1.5, 0.5).build()
        );
    }

    private static CinematicLandmarkDefinition.Builder landmark(String path, double radius, double score,
                                                                 String... tags) {
        return CinematicLandmarkDefinition.builder("idlecinematics:compat/" + path)
                .radius(radius).score(score).tags(tags);
    }
}
