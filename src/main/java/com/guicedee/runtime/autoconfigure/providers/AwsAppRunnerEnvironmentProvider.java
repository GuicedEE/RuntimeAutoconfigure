package com.guicedee.runtime.autoconfigure.providers;

import com.guicedee.runtime.autoconfigure.RuntimeEnvironment;
import com.guicedee.runtime.autoconfigure.RuntimeEnvironmentProvider;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;


/**
 * Detects AWS App Runner runtime environment.
 * <p>
 * Detection rule: {@code AWS_APP_RUNNER_SERVICE_NAME} must be present.
 * <p>
 * Mapping:
 * <ul>
 *   <li>serviceName → AWS_APP_RUNNER_SERVICE_NAME</li>
 *   <li>serviceId → AWS_APP_RUNNER_SERVICE_ID</li>
 *   <li>hostname → derived from service URL</li>
 *   <li>port → PORT or 8080</li>
 *   <li>region → AWS_REGION</li>
 * </ul>
 */
@Log4j2
public class AwsAppRunnerEnvironmentProvider extends AbstractEnvironmentProvider implements RuntimeEnvironmentProvider {

    @Override
    public boolean detected() {
        return has("AWS_APP_RUNNER_SERVICE_NAME");
    }

    @Override
    public RuntimeEnvironment runtimeEnvironment() {
        var serviceName = env("AWS_APP_RUNNER_SERVICE_NAME", "");
        var serviceId = env("AWS_APP_RUNNER_SERVICE_ID", "");
        var region = env("AWS_REGION", env("AWS_DEFAULT_REGION", ""));
        var port = envInt("PORT", 8080);

        Map<String, String> extras = new HashMap<>();
        ifPresent("AWS_APP_RUNNER_SERVICE_URL", v -> extras.put("serviceUrl", v));
        extras.put("fqdn", env("AWS_APP_RUNNER_SERVICE_URL", serviceName + ".awsapprunner.com"));

        return new RuntimeEnvironment(
                "aws-app-runner",
                serviceName,
                serviceId,
                env("AWS_APP_RUNNER_SERVICE_URL", serviceName),
                port,
                "",
                env("HOSTNAME", ""),
                region,
                "",
                Map.copyOf(extras)
        );
    }

    @Override
    public String providerId() {
        return "aws-app-runner";
    }

    @Override
    public Integer sortOrder() {
        return 95; // higher priority than generic ECS
    }
}

