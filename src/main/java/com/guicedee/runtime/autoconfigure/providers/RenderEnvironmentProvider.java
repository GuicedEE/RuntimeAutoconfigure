package com.guicedee.runtime.autoconfigure.providers;

import com.guicedee.runtime.autoconfigure.RuntimeEnvironment;
import com.guicedee.runtime.autoconfigure.RuntimeEnvironmentProvider;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;


/**
 * Detects Render runtime environment.
 * <p>
 * Detection rule: {@code RENDER_SERVICE_NAME} must be present.
 * <p>
 * Mapping:
 * <ul>
 *   <li>serviceName → RENDER_SERVICE_NAME</li>
 *   <li>serviceId → RENDER_INSTANCE_ID</li>
 *   <li>hostname → RENDER_EXTERNAL_HOSTNAME or SERVICE_NAME.onrender.com</li>
 *   <li>port → PORT or 10000 (Render default)</li>
 *   <li>region → RENDER_REGION</li>
 * </ul>
 */
@Log4j2
public class RenderEnvironmentProvider extends AbstractEnvironmentProvider implements RuntimeEnvironmentProvider {

    @Override
    public boolean detected() {
        return has("RENDER_SERVICE_NAME") || has("RENDER");
    }

    @Override
    public RuntimeEnvironment runtimeEnvironment() {
        var serviceName = env("RENDER_SERVICE_NAME", "");
        var instanceId = env("RENDER_INSTANCE_ID", "");
        var externalHostname = env("RENDER_EXTERNAL_HOSTNAME", serviceName + ".onrender.com");
        var port = envInt("PORT", 10000);

        Map<String, String> extras = new HashMap<>();
        extras.put("fqdn", externalHostname);
        ifPresent("RENDER_INTERNAL_HOSTNAME", v -> extras.put("internalHostname", v));
        ifPresent("RENDER_SERVICE_ID", v -> extras.put("serviceId", v));
        ifPresent("RENDER_GIT_COMMIT", v -> extras.put("gitCommit", v));
        ifPresent("RENDER_SERVICE_TYPE", v -> extras.put("serviceType", v));
        ifPresent("IS_PULL_REQUEST", v -> extras.put("isPullRequest", v));

        return new RuntimeEnvironment(
                "render",
                serviceName,
                instanceId,
                externalHostname,
                port,
                env("RENDER_GIT_COMMIT", ""),
                instanceId,
                env("RENDER_REGION", ""),
                "",
                Map.copyOf(extras)
        );
    }

    @Override
    public String providerId() {
        return "render";
    }

    @Override
    public Integer sortOrder() {
        return 100;
    }
}

