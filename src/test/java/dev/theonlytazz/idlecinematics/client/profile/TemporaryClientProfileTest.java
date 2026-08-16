package dev.theonlytazz.idlecinematics.client.profile;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class TemporaryClientProfileTest {
    @Test void restoresOnlyValuesStillOwnedByIdle() {
        assertEquals(120, TemporaryClientProfile.restoreIfOwned(30, 30, 120));
        assertEquals(45, TemporaryClientProfile.restoreIfOwned(45, 30, 120));
    }
}
