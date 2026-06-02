package com.guicedee.runtime.autoconfigure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generic Kubernetes platform-specific configuration.
 * <p>
 * Place on {@code package-info.java} or a class to provide Kubernetes
 * specific defaults and overrides.
 * <p>
 * Example:
 * <pre>
 * &#064;Kubernetes(
 *     namespace = "production",
 *     serviceName = "payment-service",
 *     servicePort = 8080
 * )
 * package com.myapp;
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface Kubernetes {

    /**
     * @return Override service name (if empty, derived from pod hostname).
     */
    String serviceName() default "";

    /**
     * @return Override namespace (if empty, uses KUBERNETES_NAMESPACE or service account file).
     */
    String namespace() default "";

    /**
     * @return Override service port (if 0, uses PORT env var or 8080).
     */
    int servicePort() default 0;

    /**
     * @return Cluster name (metadata for documentation/logging).
     */
    String cluster() default "";

    /**
     * @return Expected replica count (metadata for documentation).
     */
    int replicas() default 0;
}

