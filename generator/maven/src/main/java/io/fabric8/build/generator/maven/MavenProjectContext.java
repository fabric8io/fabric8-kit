package io.fabric8.build.generator.maven;

import java.io.File;

import io.fabric8.build.common.BuildLogger;
import io.fabric8.build.generator.api.ProjectContext;

import org.apache.maven.project.MavenProject;

/**
 * @author nicola
 * @since 17.07.17
 */
public class MavenProjectContext extends ProjectContext {

    private MavenProject project;

    public MavenProjectContext(MavenProject project, BuildLogger logger, boolean prePackagePhase) {
        super(project.getProperties(), logger, prePackagePhase, new MavenFromSelector.Default(), project.getVersion().endsWith("-SNAPSHOT"), project.getBasedir(), new File(project.getBuild().getDirectory()), new File(project.getBuild().getOutputDirectory()), project.getGroupId(), project.getArtifactId(), project.getVersion());
    }

    public MavenProject getProject() {
        return project;
    }
}
