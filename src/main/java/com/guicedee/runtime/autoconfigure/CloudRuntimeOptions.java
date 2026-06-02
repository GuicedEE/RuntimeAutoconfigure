package com.guicedee.runtime.autoconfigure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures cloud runtime auto-detection behavior for a package or class.
 * <p>
 * Place on {@code package-info.java} to configure detection for the application,
 * or on a specific class for targeted configuration.
 * <p>
 * All values support environment variable override using the pattern:
 * {@code CLOUD_RUNTIME_{PROPERTY}} or {@code CLOUD_RUNTIME_{NORMALIZED_NAME}_{PROPERTY}}
 * <p>
 * Example:
 * <pre>
 * &#064;CloudRuntimeOptions(
 *     preferredProvider = "azure-container-apps",
 *     defaultPort = 8080,
 *     healthPath = "/health/ready"
 * )
 * package com.myapp;
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface CloudRuntimeOptions {

    /**
     * @return Logical name for this configuration (used in env var lookups).
     */
    String value() default "default";

    /**
     * @return Preferred provider to use even if multiple are detected.
     * {@link CloudProvider#AUTO} means "first detected wins" based on sortOrder.
     */
    CloudProvider preferredProvider() default CloudProvider.AUTO;

    /**
     * @return Whether detection is enabled. Set to false to disable all auto-detection.
     */
    boolean enabled() default true;

    /**
     * @return Default port to use when no platform-specific port is detected.
     */
    int defaultPort() default 8080;

    /**
     * @return Health check path to register with service discovery / Consul.
     */
    String healthPath() default "/health/ready";

    /**
     * @return Whether to automatically register with Consul when detected values are available.
     */
    boolean autoRegisterConsul() default false;

    /**
     * @return Whether to automatically configure service resolver from detected environment.
     */
    boolean autoConfigureResolver() default true;

    /**
     * @return Override service name (if empty, uses cloud-detected value).
     */
    String serviceName() default "";

    /**
     * @return Override hostname (if empty, uses cloud-detected value).
     */
    String hostname() default "";

    /**
     * @return Override port (if 0, uses cloud-detected value or defaultPort).
     */
    int port() default 0;

    /**
     * @return Override region (if empty, uses cloud-detected value).
     */
    String region() default "";
}


