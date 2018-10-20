package io.fabric8.kit.enricher.api;

import java.util.Properties;

import io.fabric8.kit.common.KitLogger;
import io.fabric8.kit.config.resource.ResourceConfig;


/**
 * @author roland
 * @since 30.05.17
 */
public class ProjectContext {

    // project props
    private Properties properties;

    private KitLogger logger;

    private ResourceConfig resourceConfig;

    /**
     * Get a project's properties
     *
     * @return project properties
     */
    public Properties getProperties() {
        return properties;
    }

    public KitLogger getLogger() {
        return logger;
    }

    public ResourceConfig getResourceConfig() {
        return resourceConfig;
    }

    public ProjectContext(Properties properties, ResourceConfig resourceConfig, KitLogger logger) {
        this.properties = properties;
        this.logger = logger;
        this.resourceConfig = resourceConfig;
    }
}
