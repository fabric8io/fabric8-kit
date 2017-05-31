package io.fabric8.build.enricher.maven;

import io.fabric8.build.common.BuildLogger;
import io.fabric8.build.config.resource.ResourceConfig;
import io.fabric8.build.enricher.api.ProjectContext;
import org.apache.maven.project.MavenProject;

/**
 * @author roland
 * @since 30.05.17
 */
public class MavenProjectContext extends ProjectContext {

    private MavenProject project;

    public MavenProjectContext(MavenProject project, ResourceConfig resourceConfig, BuildLogger logger) {
        super(project.getProperties(), resourceConfig, logger);
    }

    public MavenProject getProject() {
        return project;
    }
}
