package com.guicedee.runtime.autoconfigure.providers;

import com.guicedee.runtime.autoconfigure.RuntimeEnvironment;
import com.guicedee.runtime.autoconfigure.RuntimeEnvironmentProvider;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;


/**
 * Detects Google App Engine runtime environment.
 * <p>
 * Detection rule: {@code GAE_APPLICATION} must be present.
 * Does NOT detect if {@code K_SERVICE} + {@code K_REVISION} are present (that's Cloud Run).
 * <p>
 * Mapping:
 * <ul>
 *   <li>serviceName → GAE_SERVICE</li>
 *   <li>serviceId → GAE_INSTANCE</li>
 *   <li>hostname → GAE_APPLICATION + ".appspot.com"</li>
 *   <li>port → PORT or 8080</li>
 *   <li>region → GAE_REGION (or from application ID)</li>
 *   <li>revision → GAE_VERSION</li>
 * </ul>
 */
@Log4j2
public class GcpAppEngineEnvironmentProvider extends AbstractEnvironmentProvider implements RuntimeEnvironmentProvider {

    @Override
    public boolean detected() {
        return has("GAE_APPLICATION") && !(has("K_SERVICE") && has("K_REVISION"));
    }

    @Override
    public RuntimeEnvironment runtimeEnvironment() {
        var application = env("GAE_APPLICATION", "");
        var service = env("GAE_SERVICE", "default");
        var version = env("GAE_VERSION", "");
        var instance = env("GAE_INSTANCE", "");
        var port = envInt("PORT", 8080);

        // GAE_APPLICATION format: "s~project-id" or "project-id"
        var projectId = application.contains("~") ? application.substring(application.indexOf('~') + 1) : application;

        Map<String, String> extras = new HashMap<>();
        extras.put("projectId", projectId);
        extras.put("application", application);
        extras.put("fqdn", service + "-dot-" + projectId + ".appspot.com");
        ifPresent("GAE_RUNTIME", v -> extras.put("runtime", v));
        ifPresent("GAE_MEMORY_MB", v -> extras.put("memoryMb", v));
        ifPresent("GAE_ENV", v -> extras.put("environment", v)); // "standard" or "flex"

        return new RuntimeEnvironment(
                "gcp-app-engine",
                service,
                instance,
                service + "-dot-" + projectId + ".appspot.com",
                port,
                version,
                instance,
                env("GAE_REGION", ""),
                "",
                Map.copyOf(extras)
        );
    }

    @Override
    public String providerId() {
        return "gcp-app-engine";
    }

    @Override
    public Integer sortOrder() {
        return 110;
    }
}

