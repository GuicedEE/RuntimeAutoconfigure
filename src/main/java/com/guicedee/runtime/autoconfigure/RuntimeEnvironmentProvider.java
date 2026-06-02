package com.guicedee.runtime.autoconfigure;

import com.guicedee.client.services.IDefaultService;

/**
 * SPI for cloud runtime environment detection.
 * <p>
 * Implementations detect whether the application is running on a specific
 * cloud platform and, if so, provide a {@link RuntimeEnvironment} populated
 * with platform-derived metadata.
 * <p>
 * Implementations are discovered via {@link java.util.ServiceLoader} and
 * evaluated in {@link IDefaultService#sortOrder()} order. The first provider
 * that returns {@code true} from {@link #detected()} wins.
 * <p>
 * Resolution precedence (merge rule):
 * <ol>
 *   <li>Explicit named environment variable</li>
 *   <li>General environment variable</li>
 *   <li>Cloud-detected value (this provider)</li>
 *   <li>Annotation default</li>
 *   <li>GuicedEE framework default</li>
 * </ol>
 */
public interface RuntimeEnvironmentProvider extends IDefaultService<RuntimeEnvironmentProvider> {

    /**
     * @return {@code true} if this provider's cloud platform is detected in the current runtime.
     */
    boolean detected();

    /**
     * @return The detected runtime environment metadata. Only called when {@link #detected()} is true.
     */
    RuntimeEnvironment runtimeEnvironment();

    /**
     * @return Short identifier for this provider (e.g. "azure-container-apps", "aws-ecs", "gcp-cloud-run").
     */
    String providerId();
}

