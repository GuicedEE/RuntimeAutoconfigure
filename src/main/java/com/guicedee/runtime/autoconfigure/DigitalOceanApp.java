package com.guicedee.runtime.autoconfigure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DigitalOcean App Platform specific configuration.
 * <p>
 * Place on {@code package-info.java} or a class to provide DigitalOcean
 * specific defaults and overrides.
 * <p>
 * Example:
 * <pre>
 * &#064;DigitalOceanApp(
 *     componentName = "web-service",
 *     region = "nyc1"
 * )
 * package com.myapp;
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface DigitalOceanApp {

    /**
     * @return Override component name (if empty, uses DIGITALOCEAN_APP_COMPONENT_NAME or APP_NAME).
     */
    String componentName() default "";

    /**
     * @return Override region slug (if empty, uses DIGITALOCEAN_REGION).
     */
    String region() default "";

    /**
     * @return Override port (if 0, uses PORT env var or 8080).
     */
    int port() default 0;

    /**
     * @return Instance count (metadata for documentation).
     */
    int instanceCount() default 0;

    /**
     * @return Instance size slug (metadata for documentation, e.g. "basic-xxs").
     */
    String instanceSize() default "";
}

