package com.guicedee.runtime.autoconfigure.providers;

import com.guicedee.runtime.autoconfigure.RuntimeEnvironment;
import com.guicedee.runtime.autoconfigure.RuntimeEnvironmentProvider;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;


/**
 * Detects Google Cloud Run runtime environment.
 * <p>
 * Detection rule: {@code K_SERVICE} and {@code K_REVISION} must be present.
 * <p>
 * Mapping:
 * <ul>
 *   <li>serviceName → K_SERVICE</li>
 *   <li>serviceId → K_REVISION</li>
 *   <li>hostname → K_SERVICE</li>
 *   <li>port → PORT (Cloud Run always sets this, default 8080)</li>
 *   <li>revision → K_REVISION</li>
 *   <li>region → GOOGLE_CLOUD_REGION or CLOUD_RUN_REGION</li>
 *   <li>extras.configuration, projectId, jobName, execution, taskIndex</li>
 * </ul>
 */
@Log4j2
public class GcpCloudRunEnvironmentProvider extends AbstractEnvironmentProvider implements RuntimeEnvironmentProvider {

    @Override
    public boolean detected() {
        return has("K_SERVICE") && has("K_REVISION");
    }

    @Override
    public RuntimeEnvironment runtimeEnvironment() {
        var serviceName = env("K_SERVICE", "");
        var revision = env("K_REVISION", "");
        var port = envInt("PORT", 8080);
        var region = env("GOOGLE_CLOUD_REGION", env("CLOUD_RUN_REGION", ""));

        Map<String, String> extras = new HashMap<>();
        ifPresent("K_CONFIGURATION", v -> extras.put("configuration", v));
        ifPresent("GOOGLE_CLOUD_PROJECT", v -> extras.put("projectId", v));
        ifPresent("CLOUD_RUN_JOB", v -> extras.put("jobName", v));
        ifPresent("CLOUD_RUN_EXECUTION", v -> extras.put("execution", v));
        ifPresent("CLOUD_RUN_TASK_INDEX", v -> extras.put("taskIndex", v));

        return new RuntimeEnvironment(
                "gcp-cloud-run",
                serviceName,
                revision,
                serviceName,
                port,
                revision,
                env("HOSTNAME", revision),
                region,
                "",
                Map.copyOf(extras)
        );
    }

    @Override
    public String providerId() {
        return "gcp-cloud-run";
    }

    @Override
    public Integer sortOrder() {
        return 100;
    }
}

