package com.guicedee.runtime.autoconfigure;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * AWS Lambda platform-specific configuration.
 * <p>
 * Place on {@code package-info.java} or a class to provide AWS Lambda
 * specific defaults and overrides.
 * <p>
 * Example:
 * <pre>
 * &#064;AwsLambda(
 *     functionName = "order-processor",
 *     memorySize = 512,
 *     timeout = 30
 * )
 * package com.myapp;
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE})
public @interface AwsLambda {

    /**
     * @return Override function name (if empty, uses AWS_LAMBDA_FUNCTION_NAME).
     */
    String functionName() default "";

    /**
     * @return Expected memory size in MB (metadata only, does not configure Lambda).
     */
    int memorySize() default 0;

    /**
     * @return Expected timeout in seconds (metadata only, does not configure Lambda).
     */
    int timeout() default 0;

    /**
     * @return Override region (if empty, uses AWS_REGION).
     */
    String region() default "";

    /**
     * @return Handler class reference (metadata for documentation/validation).
     */
    String handler() default "";
}

