package io.fabric8.build.generator.maven;

import io.fabric8.build.common.BuildLogger;
import io.fabric8.build.generator.api.FromSelector;
import io.fabric8.build.generator.api.ProjectContext;

import org.apache.maven.project.MavenProject;

/**
 * @author nicola
 * @since 17.07.17
 */
public class MavenProjectContext extends ProjectContext {

    private MavenProject project;

    public MavenProjectContext(MavenProject project, BuildLogger logger, boolean prePackagePhase, FromSelector fromSelector) {
        super(project.getProperties(), logger, prePackagePhase, fromSelector);
    }

    public MavenProject getProject() {
        return project;
    }
}
