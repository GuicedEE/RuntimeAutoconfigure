import com.guicedee.client.services.lifecycle.IGuicePreStartup;
import com.guicedee.runtime.autoconfigure.implementations.RuntimeAutoConfigurePreStartup;
import com.guicedee.runtime.autoconfigure.RuntimeEnvironmentProvider;
import com.guicedee.runtime.autoconfigure.providers.*;

module com.guicedee.runtime.autoconfigure {

    exports com.guicedee.runtime.autoconfigure;
    exports com.guicedee.runtime.autoconfigure.providers;

    requires transitive com.guicedee.client;
    requires org.apache.logging.log4j;
    requires io.vertx.core;
    requires io.github.classgraph;
    requires static lombok;

    provides IGuicePreStartup with RuntimeAutoConfigurePreStartup;

    provides RuntimeEnvironmentProvider with
            AzureContainerAppsEnvironmentProvider,
            AzureAppServiceEnvironmentProvider,
            AwsEcsEnvironmentProvider,
            AwsLambdaEnvironmentProvider,
            AwsAppRunnerEnvironmentProvider,
            AwsElasticBeanstalkEnvironmentProvider,
            GcpCloudRunEnvironmentProvider,
            GcpAppEngineEnvironmentProvider,
            DigitalOceanAppPlatformEnvironmentProvider,
            FlyIoEnvironmentProvider,
            RailwayEnvironmentProvider,
            RenderEnvironmentProvider,
            HerokuEnvironmentProvider,
            KubernetesEnvironmentProvider;

    uses RuntimeEnvironmentProvider;

    opens com.guicedee.runtime.autoconfigure to com.google.guice;
    opens com.guicedee.runtime.autoconfigure.providers to com.google.guice;

    //tests
    exports com.guicedee.runtime.autoconfigure.implementations to com.guicedee.runtime.autoconfigure.test, com.guicedee.vertx.servicediscovery.test, com.guicedee.service.registry.test;
}
