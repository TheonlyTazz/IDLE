package dev.theonlytazz.idlecinematics.config;

import org.junit.jupiter.api.Test;
import java.util.LinkedHashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

final class PresetPreferencesTest {
    @Test void disabledIdentifiersRoundTripDeterministically() {
        Set<String> parsed = PresetPreferences.parseDisabled("addon:z, idlecinematics:orbit,addon:z");
        assertEquals(Set.of("addon:z", "idlecinematics:orbit"), parsed);
        assertEquals("addon:z,idlecinematics:orbit", PresetPreferences.encodeDisabled(parsed));
    }

    @Test void legacyToggleOnlyAffectsTheFiveMatchingBuiltIns() {
        assertFalse(PresetPreferences.isEnabled("idlecinematics:breathing_orbit", Set.of(), false));
        assertTrue(PresetPreferences.isEnabled("idlecinematics:orbit", Set.of(), false));
        assertTrue(PresetPreferences.isEnabled("addon:breathing_orbit", Set.of(), false));
    }

    @Test void migratingLegacyChoiceProducesIndependentSceneToggles() {
        Set<String> disabled = new LinkedHashSet<>();
        PresetPreferences.migrateLegacyChoice(disabled, false);
        assertEquals(5, disabled.size());
        disabled.remove("idlecinematics:terrain_scout");
        assertTrue(PresetPreferences.isEnabled("idlecinematics:terrain_scout", disabled, true));
        assertFalse(PresetPreferences.isEnabled("idlecinematics:entity_portrait", disabled, true));
    }
}
