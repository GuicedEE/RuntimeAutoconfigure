package com.guicedee.runtime.autoconfigure.implementations;

import com.guicedee.client.Environment;
import com.guicedee.client.IGuiceContext;
import com.guicedee.client.services.lifecycle.IGuicePreStartup;
import com.guicedee.runtime.autoconfigure.AwsAppRunner;
import com.guicedee.runtime.autoconfigure.AwsEcs;
import com.guicedee.runtime.autoconfigure.AwsElasticBeanstalk;
import com.guicedee.runtime.autoconfigure.AwsLambda;
import com.guicedee.runtime.autoconfigure.AzureAppService;
import com.guicedee.runtime.autoconfigure.AzureContainerApps;
import com.guicedee.runtime.autoconfigure.CloudProvider;
import com.guicedee.runtime.autoconfigure.CloudRuntimeOptions;
import com.guicedee.runtime.autoconfigure.DigitalOceanApp;
import com.guicedee.runtime.autoconfigure.FlyIo;
import com.guicedee.runtime.autoconfigure.GcpAppEngine;
import com.guicedee.runtime.autoconfigure.GcpCloudRun;
import com.guicedee.runtime.autoconfigure.Heroku;
import com.guicedee.runtime.autoconfigure.Kubernetes;
import com.guicedee.runtime.autoconfigure.Railway;
import com.guicedee.runtime.autoconfigure.Render;
import com.guicedee.runtime.autoconfigure.RuntimeEnvironment;
import com.guicedee.runtime.autoconfigure.RuntimeEnvironmentProvider;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.PackageInfo;
import io.github.classgraph.ScanResult;
import io.vertx.core.Future;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Pre-startup hook that detects the cloud runtime environment.
 * <p>
 * Detection is driven by annotations found on packages or classes:
 * <ul>
 *   <li>{@link AzureContainerApps} → runs only the Azure Container Apps provider</li>
 *   <li>{@link AwsEcs} → runs only the AWS ECS provider</li>
 *   <li>{@link AwsLambda} → runs only the AWS Lambda provider</li>
 *   <li>{@link GcpCloudRun} → runs only the GCP Cloud Run provider</li>
 *   <li>{@link DigitalOceanApp} → runs only the DigitalOcean provider</li>
 *   <li>{@link Kubernetes} → runs only the Kubernetes provider</li>
 *   <li>{@link CloudRuntimeOptions} → uses {@code preferredProvider} or auto-detects from env</li>
 * </ul>
 * <p>
 * If no platform-specific annotation is found but {@link CloudRuntimeOptions} is present,
 * the {@code preferredProvider} field selects the provider. If set to {@link CloudProvider#AUTO},
 * environment variables are checked to determine the platform.
 * <p>
 * If NO annotation is found at all, no detection runs — this module is opt-in via annotation.
 * <p>
 * Override precedence:
 * <pre>
 * explicit env var > annotation value > cloud-detected value > framework default
 * </pre>
 */
@Log4j2
public class RuntimeAutoConfigurePreStartup implements IGuicePreStartup<RuntimeAutoConfigurePreStartup> {

    @Getter
    private static volatile RuntimeEnvironment detectedEnvironment;

    @Getter
    private static volatile String detectedProviderId;

    @Getter
    private static volatile CloudRuntimeOptions resolvedOptions;

    @Override
    public List<Future<Boolean>> onStartup() {
        return List.of(Future.succeededFuture(detect()));
    }

    private Boolean detect() {
        // Scan for annotations — the annotation determines which provider to run
        CloudProvider targetProvider = scanForTargetProvider();

        if (targetProvider == null) {
            log.debug("☁️  No cloud runtime annotation found — skipping detection");
            return true;
        }

        // Check if detection is disabled via @CloudRuntimeOptions or env var
        if (resolvedOptions != null && !resolvedOptions.enabled()) {
            log.info("☁️  Cloud runtime detection disabled via @CloudRuntimeOptions(enabled=false)");
            return true;
        }
        String enabledEnv = Environment.getSystemPropertyOrEnvironment("CLOUD_RUNTIME_ENABLED", "true");
        if ("false".equalsIgnoreCase(enabledEnv)) {
            log.info("☁️  Cloud runtime detection disabled via CLOUD_RUNTIME_ENABLED=false");
            return true;
        }

        // Allow env var to override the annotation-selected provider
        String envOverride = Environment.getSystemPropertyOrEnvironment("CLOUD_RUNTIME_PREFERRED_PROVIDER", "");
        if (!envOverride.isEmpty()) {
            targetProvider = CloudProvider.fromId(envOverride);
            if (targetProvider == CloudProvider.AUTO) {
                targetProvider = null; // invalid override, fall through to auto
            }
        }

        // Find and run only the matching provider
        ServiceLoader<RuntimeEnvironmentProvider> providers = ServiceLoader.load(RuntimeEnvironmentProvider.class);

        final CloudProvider finalTarget = targetProvider;
        for (var providerEntry : providers) {
            // If we have a specific target, only run that one
            if (finalTarget != null && finalTarget != CloudProvider.AUTO) {
                if (!finalTarget.id().equals(providerEntry.providerId())) {
                    continue;
                }
            }

            try {
                if (providerEntry.detected()) {
                    detectedEnvironment = applyOverrides(providerEntry.runtimeEnvironment());
                    detectedProviderId = providerEntry.providerId();
                    log.info("☁️  Cloud runtime detected: {} (provider={})", detectedProviderId, providerEntry.getClass().getName());
                    log.info("   ├─ serviceName : {}", detectedEnvironment.serviceName());
                    log.info("   ├─ serviceId   : {}", detectedEnvironment.serviceId());
                    log.info("   ├─ hostname    : {}", detectedEnvironment.hostname());
                    log.info("   ├─ port        : {}", detectedEnvironment.port());
                    log.info("   ├─ region      : {}", detectedEnvironment.region());
                    log.info("   ├─ revision    : {}", detectedEnvironment.revision());
                    log.info("   └─ replicaName : {}", detectedEnvironment.replicaName());
                    return true;
                }
            } catch (Exception e) {
                log.debug("Provider {} threw during detection: {}", providerEntry.getClass().getName(), e.getMessage());
            }

            // If we targeted a specific provider and it didn't detect, stop
            if (finalTarget != null && finalTarget != CloudProvider.AUTO) {
                log.debug("☁️  Targeted provider '{}' did not detect — not running in that environment", finalTarget.id());
                return true;
            }
        }

        log.debug("☁️  No cloud runtime detected — running in local/unknown environment");
        return true;
    }

    /**
     * Scans for platform-specific annotations to determine which provider to invoke.
     * <p>
     * Priority:
     * <ol>
     *   <li>Platform-specific annotations (@AzureContainerApps, @AwsLambda, etc.) → specific provider</li>
     *   <li>@CloudRuntimeOptions with preferredProvider set → that provider</li>
     *   <li>@CloudRuntimeOptions with AUTO → auto-detect from environment</li>
     *   <li>No annotation → null (skip detection entirely)</li>
     * </ol>
     */
    private CloudProvider scanForTargetProvider() {
        try {
            ScanResult scanResult = IGuiceContext.instance().getScanResult();
            if (scanResult == null) return null;

            // Check for platform-specific annotations first
            CloudProvider platformTarget = scanPlatformAnnotations(scanResult);
            if (platformTarget != null) return platformTarget;

            // Fall back to @CloudRuntimeOptions
            resolvedOptions = scanCloudRuntimeOptions(scanResult);
            if (resolvedOptions != null) {
                return resolvedOptions.preferredProvider();
            }

        } catch (Exception e) {
            log.debug("Annotation scanning unavailable: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Scans for platform-specific annotations and returns the corresponding provider.
     */
    private CloudProvider scanPlatformAnnotations(ScanResult scanResult) {
        // Map each annotation to its provider
        record AnnotationMapping(Class<? extends Annotation> annotationType, CloudProvider provider) {}

        var mappings = List.of(
                new AnnotationMapping(AzureContainerApps.class, CloudProvider.AZURE_CONTAINER_APPS),
                new AnnotationMapping(AzureAppService.class, CloudProvider.AZURE_APP_SERVICE),
                new AnnotationMapping(AwsEcs.class, CloudProvider.AWS_ECS),
                new AnnotationMapping(AwsLambda.class, CloudProvider.AWS_LAMBDA),
                new AnnotationMapping(AwsAppRunner.class, CloudProvider.AWS_APP_RUNNER),
                new AnnotationMapping(AwsElasticBeanstalk.class, CloudProvider.AWS_ELASTIC_BEANSTALK),
                new AnnotationMapping(GcpCloudRun.class, CloudProvider.GCP_CLOUD_RUN),
                new AnnotationMapping(GcpAppEngine.class, CloudProvider.GCP_APP_ENGINE),
                new AnnotationMapping(DigitalOceanApp.class, CloudProvider.DIGITALOCEAN_APP_PLATFORM),
                new AnnotationMapping(FlyIo.class, CloudProvider.FLY_IO),
                new AnnotationMapping(Railway.class, CloudProvider.RAILWAY),
                new AnnotationMapping(Render.class, CloudProvider.RENDER),
                new AnnotationMapping(Heroku.class, CloudProvider.HEROKU),
                new AnnotationMapping(Kubernetes.class, CloudProvider.KUBERNETES)
        );

        for (var mapping : mappings) {
            // Check packages
            for (PackageInfo packageInfo : scanResult.getPackageInfo()) {
                if (packageInfo.getAnnotationInfo(mapping.annotationType.getName()) != null) {
                    log.debug("📋 Found @{} on package '{}'", mapping.annotationType.getSimpleName(), packageInfo.getName());
                    return mapping.provider;
                }
            }
            // Check classes
            var classesWithAnnotation = scanResult.getClassesWithAnnotation(mapping.annotationType);
            for (ClassInfo classInfo : classesWithAnnotation) {
                if (classInfo.getName().endsWith(".package-info")) continue;
                log.debug("📋 Found @{} on class '{}'", mapping.annotationType.getSimpleName(), classInfo.getName());
                return mapping.provider;
            }
        }
        return null;
    }

    /**
     * Scans for @CloudRuntimeOptions annotation on packages or classes.
     */
    private CloudRuntimeOptions scanCloudRuntimeOptions(ScanResult scanResult) {
        for (PackageInfo packageInfo : scanResult.getPackageInfo()) {
            var annotationInfo = packageInfo.getAnnotationInfo(CloudRuntimeOptions.class.getName());
            if (annotationInfo != null) {
                try {
                    Class<?> packageInfoClass = Class.forName(packageInfo.getName() + ".package-info");
                    CloudRuntimeOptions annotation = packageInfoClass.getAnnotation(CloudRuntimeOptions.class);
                    if (annotation != null) {
                        log.debug("📋 Found @CloudRuntimeOptions on package '{}'", packageInfo.getName());
                        return annotation;
                    }
                } catch (ClassNotFoundException e) {
                    log.debug("Could not load package-info for {}", packageInfo.getName());
                }
            }
        }

        var classesWithAnnotation = scanResult.getClassesWithAnnotation(CloudRuntimeOptions.class);
        for (var classInfo : classesWithAnnotation) {
            if (classInfo.getName().endsWith(".package-info")) continue;
            try {
                Class<?> clazz = classInfo.loadClass();
                CloudRuntimeOptions annotation = clazz.getAnnotation(CloudRuntimeOptions.class);
                if (annotation != null) {
                    log.debug("📋 Found @CloudRuntimeOptions on class '{}'", classInfo.getName());
                    return annotation;
                }
            } catch (Exception e) {
                log.debug("Error loading @CloudRuntimeOptions from {}", classInfo.getName());
            }
        }
        return null;
    }

    /**
     * Applies annotation overrides and env var overrides on top of detected values.
     */
    private RuntimeEnvironment applyOverrides(RuntimeEnvironment detected) {
        String serviceName = Environment.getSystemPropertyOrEnvironment("CLOUD_RUNTIME_SERVICE_NAME",
                resolvedOptions != null && !resolvedOptions.serviceName().isEmpty()
                        ? resolvedOptions.serviceName() : detected.serviceName());

        String hostname = Environment.getSystemPropertyOrEnvironment("CLOUD_RUNTIME_HOSTNAME",
                resolvedOptions != null && !resolvedOptions.hostname().isEmpty()
                        ? resolvedOptions.hostname() : detected.hostname());

        int port = envInt("CLOUD_RUNTIME_PORT",
                resolvedOptions != null && resolvedOptions.port() > 0
                        ? resolvedOptions.port() : detected.port());

        String region = Environment.getSystemPropertyOrEnvironment("CLOUD_RUNTIME_REGION",
                resolvedOptions != null && !resolvedOptions.region().isEmpty()
                        ? resolvedOptions.region() : detected.region());

        if (serviceName.equals(detected.serviceName())
                && hostname.equals(detected.hostname())
                && port == detected.port()
                && region.equals(detected.region())) {
            return detected; // no overrides applied
        }

        return new RuntimeEnvironment(
                detected.provider(),
                serviceName,
                detected.serviceId(),
                hostname,
                port,
                detected.revision(),
                detected.replicaName(),
                region,
                detected.zone(),
                detected.extras()
        );
    }


    /**
     * @return The detected environment if a provider matched, or empty.
     */
    public static Optional<RuntimeEnvironment> current() {
        return Optional.ofNullable(detectedEnvironment);
    }

    /**
     * @return true if any cloud runtime was detected.
     */
    public static boolean isCloudDetected() {
        return detectedEnvironment != null;
    }

    private static int envInt(String key, int defaultValue) {
        String value = Environment.getSystemPropertyOrEnvironment(key, "");
        if (value.isEmpty()) return defaultValue;
        try { return Integer.parseInt(value); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    @Override
    public Integer sortOrder() {
        return Integer.MIN_VALUE + 50;
    }
}

