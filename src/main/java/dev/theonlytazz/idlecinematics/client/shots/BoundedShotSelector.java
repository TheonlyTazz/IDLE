package dev.theonlytazz.idlecinematics.client.shots;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class BoundedShotSelector {
    private BoundedShotSelector() {}

    public static <T> T choose(int attempts, Supplier<T> candidates, Predicate<T> validator, Supplier<T> fallback) {
        Objects.requireNonNull(candidates, "candidates"); Objects.requireNonNull(validator, "validator"); Objects.requireNonNull(fallback, "fallback");
        for (int index = 0; index < Math.max(0, attempts); index++) {
            T candidate = candidates.get();
            if (validator.test(candidate)) return candidate;
        }
        return fallback.get();
    }
}
