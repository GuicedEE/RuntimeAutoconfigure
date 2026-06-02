package com.guicedee.runtime.autoconfigure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AWS ECS / Fargate platform-specific configuration.
 * <p>
 * Place on {@code package-info.java} or a class to provide AWS ECS/Fargate
 * specific defaults and overrides.
 * <p>
 * Example:
 * <pre>
 * &#064;AwsEcs(
 *     serviceName = "payment-service",
 *     cluster = "prod-cluster",
 *     launchType = AwsEcs.LaunchType.FARGATE
 * )
 * package com.myapp;
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface AwsEcs {

    /**
     * ECS launch type.
     */
    enum LaunchType {
        EC2, FARGATE
    }

    /**
     * @return Override service name (if empty, uses ECS_SERVICE_NAME or HOSTNAME).
     */
    String serviceName() default "";

    /**
     * @return ECS cluster name (metadata for documentation/validation).
     */
    String cluster() default "";

    /**
     * @return Expected launch type (metadata for documentation/validation).
     */
    LaunchType launchType() default LaunchType.FARGATE;

    /**
     * @return Override region (if empty, uses AWS_REGION).
     */
    String region() default "";

    /**
     * @return Override port (if 0, uses PORT env var or 8080).
     */
    int port() default 0;

    /**
     * @return Desired task count (metadata for health/scaling documentation).
     */
    int desiredCount() default 0;
}

