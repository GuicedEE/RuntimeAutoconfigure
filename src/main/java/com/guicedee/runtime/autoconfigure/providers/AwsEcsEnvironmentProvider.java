package com.guicedee.runtime.autoconfigure.providers;

import com.guicedee.runtime.autoconfigure.RuntimeEnvironment;
import com.guicedee.runtime.autoconfigure.RuntimeEnvironmentProvider;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;


/**
 * Detects AWS ECS / Fargate runtime environment.
 * <p>
 * Detection rule: {@code ECS_CONTAINER_METADATA_URI_V4} or {@code ECS_CONTAINER_METADATA_URI} must be present.
 * <p>
 * Mapping:
 * <ul>
 *   <li>serviceName → ECS_SERVICE_NAME</li>
 *   <li>serviceId → ECS_TASK_ARN or HOSTNAME</li>
 *   <li>hostname → HOSTNAME</li>
 *   <li>port → PORT or 8080</li>
 *   <li>region → AWS_REGION or AWS_DEFAULT_REGION</li>
 *   <li>extras.taskArn, clusterArn, launchType, metadataUri</li>
 * </ul>
 */
@Log4j2
public class AwsEcsEnvironmentProvider extends AbstractEnvironmentProvider implements RuntimeEnvironmentProvider {

    @Override
    public boolean detected() {
        return has("ECS_CONTAINER_METADATA_URI_V4") || has("ECS_CONTAINER_METADATA_URI");
    }

    @Override
    public RuntimeEnvironment runtimeEnvironment() {
        var serviceName = env("ECS_SERVICE_NAME", env("AWS_LAMBDA_FUNCTION_NAME", env("HOSTNAME", "unknown")));
        var taskArn = env("ECS_TASK_ARN", "");
        var cluster = env("ECS_CLUSTER", "");
        var region = env("AWS_REGION", env("AWS_DEFAULT_REGION", ""));
        var az = env("AWS_AVAILABILITY_ZONE", "");
        var hostname = env("HOSTNAME", "");
        var port = envInt("PORT", 8080);

        Map<String, String> extras = new HashMap<>();
        extras.put("taskArn", taskArn);
        extras.put("clusterArn", cluster);
        ifPresent("ECS_LAUNCH_TYPE", v -> extras.put("launchType", v));
        ifPresent("ECS_CONTAINER_METADATA_URI_V4", v -> extras.put("metadataUri", v));
        ifPresent("AWS_EXECUTION_ENV", v -> extras.put("executionEnv", v));

        return new RuntimeEnvironment(
                "aws-ecs",
                serviceName,
                taskArn.isEmpty() ? hostname : taskArn,
                hostname,
                port,
                "",
                hostname,
                region,
                az,
                Map.copyOf(extras)
        );
    }

    @Override
    public String providerId() {
        return "aws-ecs";
    }

    @Override
    public Integer sortOrder() {
        return 100;
    }
}

