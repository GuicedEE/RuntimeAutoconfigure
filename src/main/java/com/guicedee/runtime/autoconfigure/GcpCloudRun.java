package com.guicedee.runtime.autoconfigure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Google Cloud Run platform-specific configuration.
 * <p>
 * Place on {@code package-info.java} or a class to provide GCP Cloud Run
 * specific defaults and overrides.
 * <p>
 * Example:
 * <pre>
 * &#064;GcpCloudRun(
 *     serviceName = "payments",
 *     region = "us-central1",
 *     concurrency = 80
 * )
 * package com.myapp;
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface GcpCloudRun {

    /**
     * @return Override service name (if empty, uses K_SERVICE).
     */
    String serviceName() default "";

    /**
     * @return Override region (if empty, uses GOOGLE_CLOUD_REGION).
     */
    String region() default "";

    /**
     * @return GCP project ID (if empty, uses GOOGLE_CLOUD_PROJECT).
     */
    String projectId() default "";

    /**
     * @return Override port (if 0, uses PORT env var or 8080).
     */
    int port() default 0;

    /**
     * @return Max request concurrency per instance (metadata for documentation).
     */
    int concurrency() default 0;

    /**
     * @return Min instances (metadata for scaling documentation).
     */
    int minInstances() default 0;

    /**
     * @return Max instances (metadata for scaling documentation).
     */
    int maxInstances() default 0;

    /**
     * @return Request timeout in seconds (metadata for documentation).
     */
    int timeout() default 0;
}

