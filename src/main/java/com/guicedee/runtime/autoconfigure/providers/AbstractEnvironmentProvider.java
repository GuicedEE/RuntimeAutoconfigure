package com.guicedee.runtime.autoconfigure.providers;

import com.guicedee.client.Environment;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Base class for runtime environment providers with convenience methods
 * that delegate to {@link com.guicedee.client.Environment}.
 */
public abstract class AbstractEnvironmentProvider {

    /**
     * Gets a property value from system properties or environment variables.
     */
    protected static String env(String key, String defaultValue) {
        return Environment.getSystemPropertyOrEnvironment(key, defaultValue);
    }

    /**
     * Gets a property value, returns empty string if not found.
     */
    protected static String env(String key) {
        return Environment.getSystemPropertyOrEnvironment(key, "");
    }

    /**
     * If the property exists and is non-empty, applies it to the consumer.
     */
    protected static void ifPresent(String key, Consumer<String> consumer) {
        String value = Environment.getSystemPropertyOrEnvironment(key, "");
        if (!value.isEmpty()) {
            consumer.accept(value);
        }
    }

    /**
     * Returns true if the property/env var exists and is non-empty.
     */
    protected static boolean has(String key) {
        return !Environment.getSystemPropertyOrEnvironment(key, "").isEmpty();
    }

    /**
     * Parses an integer from a property, returning defaultValue if missing or unparseable.
     */
    protected static int envInt(String key, int defaultValue) {
        String value = Environment.getSystemPropertyOrEnvironment(key, "");
        if (value.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

