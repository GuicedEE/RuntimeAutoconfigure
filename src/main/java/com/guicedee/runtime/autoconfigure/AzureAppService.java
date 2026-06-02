package com.guicedee.runtime.autoconfigure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Azure App Service / Azure Functions platform-specific configuration.
 * <p>
 * Place on {@code package-info.java} or a class to target the Azure App Service provider.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface AzureAppService {

    String siteName() default "";
    String slotName() default "";
    int port() default 0;
    String region() default "";
}

