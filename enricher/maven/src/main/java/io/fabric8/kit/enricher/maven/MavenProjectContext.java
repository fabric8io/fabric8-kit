package io.fabric8.kit.enricher.maven;

import io.fabric8.kit.common.KitLogger;
import io.fabric8.kit.config.resource.ResourceConfig;
import io.fabric8.kit.enricher.api.ProjectContext;
import org.apache.maven.project.MavenProject;

/**
 * @author roland
 * @since 30.05.17
 */
public class MavenProjectContext extends ProjectContext {

    private MavenProject project;

    public MavenProjectContext(MavenProject project, ResourceConfig resourceConfig, KitLogger logger) {
        super(project.getProperties(), resourceConfig, logger);
    }

    public MavenProject getProject() {
        return project;
    }
}
