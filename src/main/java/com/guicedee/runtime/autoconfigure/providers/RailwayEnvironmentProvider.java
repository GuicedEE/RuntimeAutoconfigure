package com.guicedee.runtime.autoconfigure.providers;

import com.guicedee.runtime.autoconfigure.RuntimeEnvironment;
import com.guicedee.runtime.autoconfigure.RuntimeEnvironmentProvider;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;


/**
 * Detects Railway runtime environment.
 * <p>
 * Detection rule: {@code RAILWAY_SERVICE_NAME} must be present.
 * <p>
 * Mapping:
 * <ul>
 *   <li>serviceName → RAILWAY_SERVICE_NAME</li>
 *   <li>serviceId → RAILWAY_REPLICA_ID</li>
 *   <li>hostname → RAILWAY_PUBLIC_DOMAIN or RAILWAY_PRIVATE_DOMAIN</li>
 *   <li>port → PORT or 8080</li>
 *   <li>region → RAILWAY_REGION</li>
 * </ul>
 */
@Log4j2
public class RailwayEnvironmentProvider extends AbstractEnvironmentProvider implements RuntimeEnvironmentProvider {

    @Override
    public boolean detected() {
        return has("RAILWAY_SERVICE_NAME");
    }

    @Override
    public RuntimeEnvironment runtimeEnvironment() {
        var serviceName = env("RAILWAY_SERVICE_NAME", "");
        var replicaId = env("RAILWAY_REPLICA_ID", "");
        var port = envInt("PORT", 8080);
        var publicDomain = env("RAILWAY_PUBLIC_DOMAIN", "");
        var privateDomain = env("RAILWAY_PRIVATE_DOMAIN", serviceName + ".railway.internal");
        var hostname = publicDomain.isEmpty() ? privateDomain : publicDomain;

        Map<String, String> extras = new HashMap<>();
        extras.put("fqdn", hostname);
        extras.put("privateDomain", privateDomain);
        ifPresent("RAILWAY_PUBLIC_DOMAIN", v -> extras.put("publicDomain", v));
        ifPresent("RAILWAY_ENVIRONMENT_NAME", v -> extras.put("environmentName", v));
        ifPresent("RAILWAY_PROJECT_ID", v -> extras.put("projectId", v));
        ifPresent("RAILWAY_DEPLOYMENT_ID", v -> extras.put("deploymentId", v));

        return new RuntimeEnvironment(
                "railway",
                serviceName,
                replicaId,
                hostname,
                port,
                env("RAILWAY_DEPLOYMENT_ID", ""),
                replicaId,
                env("RAILWAY_REGION", ""),
                "",
                Map.copyOf(extras)
        );
    }

    @Override
    public String providerId() {
        return "railway";
    }

    @Override
    public Integer sortOrder() {
        return 100;
    }
}

