package dev.theonlytazz.idlecinematics.core;

import java.util.Objects;
import java.util.regex.Pattern;

public record NamespacedId(String namespace, String path) {
    private static final Pattern NAMESPACE = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern PATH = Pattern.compile("[a-z0-9_./-]+");

    public NamespacedId {
        Objects.requireNonNull(namespace, "namespace");
        Objects.requireNonNull(path, "path");
        if (!NAMESPACE.matcher(namespace).matches() || !PATH.matcher(path).matches()) {
            throw new IllegalArgumentException("Invalid namespaced identifier: " + namespace + ':' + path);
        }
    }

    public static NamespacedId parse(String value) {
        int separator = Objects.requireNonNull(value, "value").indexOf(':');
        if (separator <= 0 || separator == value.length() - 1 || value.indexOf(':', separator + 1) >= 0) {
            throw new IllegalArgumentException("Cinematic preset identifiers must be namespaced: " + value);
        }
        return new NamespacedId(value.substring(0, separator), value.substring(separator + 1));
    }

    @Override public String toString() { return namespace + ':' + path; }
}
