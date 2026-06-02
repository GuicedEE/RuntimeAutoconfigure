package com.guicedee.runtime.autoconfigure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Google App Engine platform-specific configuration.
 * <p>
 * Place on {@code package-info.java} or a class to target the GCP App Engine provider.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface GcpAppEngine {

    String serviceName() default "";
    String projectId() default "";
    int port() default 0;
    String region() default "";
}

