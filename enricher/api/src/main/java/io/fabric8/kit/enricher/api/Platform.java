package io.fabric8.kit.enricher.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author roland
 * @since 29.05.17
 */
public enum Platform {

    KUBERNETES(
        "Deployment",
        "Namespace",
        "Ingress"
    ),

    OPENSHIFT(
        "DeploymentConfig",
        "Project",
        "Route"
    );

    private final Set<String> unsupportedResources;

    Platform(String ... resources) {
        unsupportedResources = new HashSet<>(Arrays.asList(resources));
    }

    public boolean supportsKind(String kind) {
        return !unsupportedResources.contains(kind);
    }
}
