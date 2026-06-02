package com.guicedee.runtime.autoconfigure;

import java.util.Map;
import java.util.Optional;

/**
 * Immutable record representing the detected cloud runtime environment.
 * <p>
 * Provides metadata about where the application is running, including
 * service identity, networking, and platform-specific attributes.
 */
public record RuntimeEnvironment(
        String provider,
        String serviceName,
        String serviceId,
        String hostname,
        Integer port,
        String revision,
        String replicaName,
        String region,
        String zone,
        Map<String, String> extras
) {
    /**
     * Convenience: returns the service FQDN if available, otherwise the hostname.
     */
    public String fqdn() {
        String fqdn = extras().getOrDefault("fqdn", "");
        return fqdn.isEmpty() ? hostname() : fqdn;
    }

    /**
     * Convenience: returns an extra value or empty.
     */
    public Optional<String> extra(String key) {
        return Optional.ofNullable(extras().get(key));
    }
}

