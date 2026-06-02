package com.guicedee.runtime.autoconfigure.providers;

import com.guicedee.runtime.autoconfigure.RuntimeEnvironment;
import com.guicedee.runtime.autoconfigure.RuntimeEnvironmentProvider;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;


/**
 * Detects generic Kubernetes runtime environment.
 * <p>
 * Detection rule: {@code KUBERNETES_SERVICE_HOST} must be present (injected into every pod by kubelet).
 * <p>
 * This provider has the lowest priority (sortOrder=200) so that more specific
 * providers (Azure Container Apps, GCP Cloud Run, AWS ECS) win when applicable.
 * <p>
 * Mapping:
 * <ul>
 *   <li>serviceName → derived from HOSTNAME (strips replicaset + pod hash)</li>
 *   <li>serviceId → HOSTNAME (pod name)</li>
 *   <li>hostname → HOSTNAME</li>
 *   <li>port → PORT or 8080</li>
 *   <li>extras.namespace, podName, nodeName, apiHost, apiPort, podIp</li>
 * </ul>
 */
@Log4j2
public class KubernetesEnvironmentProvider extends AbstractEnvironmentProvider implements RuntimeEnvironmentProvider {

    @Override
    public boolean detected() {
        return has("KUBERNETES_SERVICE_HOST");
    }

    @Override
    public RuntimeEnvironment runtimeEnvironment() {
        var hostname = env("HOSTNAME", "");
        var port = envInt("PORT", 8080);
        var namespace = env("KUBERNETES_NAMESPACE", readNamespaceFile());

        Map<String, String> extras = new HashMap<>();
        extras.put("namespace", namespace);
        extras.put("podName", hostname);
        ifPresent("KUBERNETES_NODE_NAME", v -> extras.put("nodeName", v));
        ifPresent("KUBERNETES_SERVICE_HOST", v -> extras.put("apiHost", v));
        ifPresent("KUBERNETES_SERVICE_PORT", v -> extras.put("apiPort", v));
        ifPresent("KUBERNETES_POD_IP", v -> extras.put("podIp", v));

        var serviceName = deriveServiceName(hostname);

        return new RuntimeEnvironment(
                "kubernetes",
                serviceName,
                hostname,
                hostname,
                port,
                "",
                hostname,
                "",
                "",
                Map.copyOf(extras)
        );
    }

    @Override
    public String providerId() {
        return "kubernetes";
    }

    @Override
    public Integer sortOrder() {
        return 200; // lowest priority — more specific providers should match first
    }

    private String readNamespaceFile() {
        try {
            return java.nio.file.Files.readString(
                    java.nio.file.Path.of("/var/run/secrets/kubernetes.io/serviceaccount/namespace")).trim();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Best-effort derivation of service name from pod hostname.
     * Pod names typically follow: {deployment}-{replicaset-hash}-{pod-hash}
     */
    private String deriveServiceName(String podName) {
        if (podName == null || podName.isEmpty()) return "";
        String[] parts = podName.split("-");
        if (parts.length > 2) {
            var sb = new StringBuilder();
            for (int i = 0; i < parts.length - 2; i++) {
                if (i > 0) sb.append('-');
                sb.append(parts[i]);
            }
            return sb.toString();
        }
        return podName;
    }
}

