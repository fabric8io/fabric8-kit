package io.fabric8.build.enricher.maven;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import io.fabric8.build.enricher.api.EnricherManager;


/**
 * @author roland
 * @since 30.05.17
 */
public class MavenEnricherManager extends EnricherManager<MavenProjectContext> {

    private static final String[] ENRICHER_RESOURCES = new String[] {
        "META-INF/fabric8-enricher-default",
        "META-INF/fabric8/enricher-default",
        "META-INF/fabric8-enricher",
        "META-INF/fabric8/enricher"
    };

    public MavenEnricherManager(MavenProjectContext projectContext, MavenEnrichersConfig enrichersConfig) {
        super(projectContext, enrichersConfig, ENRICHER_RESOURCES, createProjectClassLoader(projectContext));
    }

    private static ClassLoader[] createProjectClassLoader(MavenProjectContext projectContext) {
        try {
            List<URL> compileJars = new ArrayList<>();

            for (String element : projectContext.getProject().getCompileClasspathElements()) {
                compileJars.add(new File(element).toURI().toURL());
            }
            return new ClassLoader[]{
                new URLClassLoader(compileJars.toArray(new URL[compileJars.size()]),
                                   MavenEnricherManager.class.getClassLoader())
            };

        } catch (Exception e) {
            projectContext.getLogger().warn("Instructed to use project classpath, but cannot. Continuing build if we can: ", e);
            return new ClassLoader[0];
        }
    }
}