package io.fabric8.build.enricher.api;

import java.util.Map;

/**
 * @author roland
 * @since 30.05.17
 */
public class EnricherContext<P extends ProjectContext> {

    private P projectContext;

    // Custom configuration for this enricher
    private Map<String, String> config;

    /**
     * Get the configuration for this specific enricher
     */
    public Map<String, String> getConfig() {
        return config;
    }

    public P getProjectContext() {
        return projectContext;
    }

    EnricherContext(P projectContext, Map<String, String> config) {
        this.projectContext = projectContext;
        this.config = config;
    }
}
