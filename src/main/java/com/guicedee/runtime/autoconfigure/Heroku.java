package com.guicedee.runtime.autoconfigure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Heroku platform-specific configuration.
 * <p>
 * Place on {@code package-info.java} or a class to target the Heroku provider.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface Heroku {

    String appName() default "";
    int port() default 0;
}

