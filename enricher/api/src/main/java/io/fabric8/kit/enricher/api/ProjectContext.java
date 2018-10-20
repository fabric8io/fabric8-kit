package io.fabric8.kit.enricher.api;

import java.util.Properties;

import io.fabric8.kit.common.BuildLogger;
import io.fabric8.kit.config.resource.ResourceConfig;


/**
 * @author roland
 * @since 30.05.17
 */
public class ProjectContext {

    // project props
    private Properties properties;

    private BuildLogger logger;

    private ResourceConfig resourceConfig;

    /**
     * Get a project's properties
     *
     * @return project properties
     */
    public Properties getProperties() {
        return properties;
    }

    public BuildLogger getLogger() {
        return logger;
    }

    public ResourceConfig getResourceConfig() {
        return resourceConfig;
    }

    public ProjectContext(Properties properties, ResourceConfig resourceConfig, BuildLogger logger) {
        this.properties = properties;
        this.logger = logger;
        this.resourceConfig = resourceConfig;
    }
}
