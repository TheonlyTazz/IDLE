package dev.theonlytazz.idlecinematics.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class CriticallyDampedValueTest {
    @Test void convergesAcrossFrameRates() {
        for (int fps : new int[]{20, 60, 144}) {
            CriticallyDampedValue value = new CriticallyDampedValue(0);
            for (int frame = 0; frame < fps * 4; frame++) value.update(10, 1.0 / fps, 0.35);
            assertEquals(10, value.value(), 1.0e-4, "fps=" + fps);
            assertEquals(0, value.velocity(), 1.0e-4, "fps=" + fps);
        }
    }

    @Test void clampsStallsAndRejectsInvalidTargets() {
        CriticallyDampedValue value = new CriticallyDampedValue(2);
        value.update(10, 30, 0.35);
        assertTrue(value.value() > 2 && value.value() < 10);
        value.update(Double.NaN, 0.05, 0.35);
        assertTrue(Double.isFinite(value.value()));
    }

    @Test void wrapsYawAndContinuesForward() {
        assertEquals(2, CriticallyDampedValue.shortestDelta(179, -179), 1.0e-8);
        CriticallyDampedValue value = new CriticallyDampedValue(350);
        for (int i = 0; i < 20; i++) value.updateAngle(10, 0.05, 0.28, true);
        assertTrue(value.value() > 350);
    }

    @Test void irregularFramesStillConverge() {
        CriticallyDampedValue value = new CriticallyDampedValue(-4);
        double[] frames = {0.016, 0.033, 0.008, 0.2, 0.011};
        for (int i = 0; i < 300; i++) value.update(7, frames[i % frames.length], 0.4);
        assertEquals(7, value.value(), 1.0e-4);
    }
}
