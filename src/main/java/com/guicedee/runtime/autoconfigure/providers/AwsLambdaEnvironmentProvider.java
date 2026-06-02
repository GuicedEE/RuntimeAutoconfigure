package com.guicedee.runtime.autoconfigure.providers;

import com.guicedee.runtime.autoconfigure.RuntimeEnvironment;
import com.guicedee.runtime.autoconfigure.RuntimeEnvironmentProvider;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;


/**
 * Detects AWS Lambda runtime environment.
 * <p>
 * Detection rule: {@code AWS_LAMBDA_FUNCTION_NAME} must be present AND
 * {@code ECS_CONTAINER_METADATA_URI_V4} must NOT be present (to avoid conflict with ECS).
 * <p>
 * Mapping:
 * <ul>
 *   <li>serviceName → AWS_LAMBDA_FUNCTION_NAME</li>
 *   <li>serviceId → AWS_LAMBDA_LOG_STREAM_NAME</li>
 *   <li>hostname → AWS_LAMBDA_FUNCTION_NAME</li>
 *   <li>port → 8080 (not applicable for Lambda)</li>
 *   <li>region → AWS_REGION</li>
 *   <li>extras.functionVersion, memorySize, handler, runtimeApi</li>
 * </ul>
 */
@Log4j2
public class AwsLambdaEnvironmentProvider extends AbstractEnvironmentProvider implements RuntimeEnvironmentProvider {

    @Override
    public boolean detected() {
        return has("AWS_LAMBDA_FUNCTION_NAME") && !has("ECS_CONTAINER_METADATA_URI_V4");
    }

    @Override
    public RuntimeEnvironment runtimeEnvironment() {
        var functionName = env("AWS_LAMBDA_FUNCTION_NAME", "");
        var logStream = env("AWS_LAMBDA_LOG_STREAM_NAME", "");
        var region = env("AWS_REGION", env("AWS_DEFAULT_REGION", ""));
        var version = env("AWS_LAMBDA_FUNCTION_VERSION", "$LATEST");

        Map<String, String> extras = new HashMap<>();
        extras.put("functionVersion", version);
        ifPresent("AWS_LAMBDA_FUNCTION_MEMORY_SIZE", v -> extras.put("memorySize", v));
        ifPresent("_HANDLER", v -> extras.put("handler", v));
        ifPresent("AWS_LAMBDA_RUNTIME_API", v -> extras.put("runtimeApi", v));

        return new RuntimeEnvironment(
                "aws-lambda",
                functionName,
                logStream,
                functionName,
                8080,
                version,
                logStream,
                region,
                "",
                Map.copyOf(extras)
        );
    }

    @Override
    public String providerId() {
        return "aws-lambda";
    }

    @Override
    public Integer sortOrder() {
        return 90;
    }
}

