package com.guicedee.runtime.autoconfigure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Azure Container Apps platform-specific configuration.
 * <p>
 * Place on {@code package-info.java} or a class to provide Azure Container Apps
 * specific defaults and overrides. Values support {@code ${ENV_VAR:default}} placeholders.
 * <p>
 * Example:
 * <pre>
 * &#064;AzureContainerApps(
 *     appName = "wallet-api",
 *     internalOnly = true
 * )
 * package com.myapp;
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface AzureContainerApps {

    /**
     * @return Override app name (if empty, uses CONTAINER_APP_NAME env var).
     */
    String appName() default "";

    /**
     * @return Override environment DNS suffix (if empty, uses CONTAINER_APP_ENV_DNS_SUFFIX).
     */
    String dnsSuffix() default "";

    /**
     * @return Override the port (if 0, uses CONTAINER_APP_PORT or 8080).
     */
    int port() default 0;

    /**
     * @return Whether this app is internal-only (no external ingress).
     * Affects FQDN construction and service resolution.
     */
    boolean internalOnly() default false;

    /**
     * @return Target Azure region (if empty, uses AZURE_REGION or auto-detected).
     */
    String region() default "";

    /**
     * @return Minimum number of replicas for health registration metadata.
     */
    int minReplicas() default 0;

    /**
     * @return Maximum number of replicas for health registration metadata.
     */
    int maxReplicas() default 0;
}

