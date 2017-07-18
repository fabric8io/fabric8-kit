package io.fabric8.build.generator.maven;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import io.fabric8.build.generator.api.GeneratorManager;


/**
 * @author nicola
 * @since 17.07.17
 */
public class MavenGeneratorManager extends GeneratorManager<MavenProjectContext> {

    private static final String[] GENERATOR_RESOURCES = new String[]{
            "META-INF/fabric8-generator-default",
            "META-INF/fabric8/generator-default",
            "META-INF/fabric8-generator",
            "META-INF/fabric8/generator"
    };

    public MavenGeneratorManager(MavenProjectContext projectContext, MavenGeneratorsConfig generatorsConfig) {
        super(projectContext, generatorsConfig, GENERATOR_RESOURCES, createProjectClassLoader(projectContext));
    }

    private static ClassLoader[] createProjectClassLoader(MavenProjectContext projectContext) {
        try {
            List<URL> compileJars = new ArrayList<>();

            for (String element : projectContext.getProject().getCompileClasspathElements()) {
                compileJars.add(new File(element).toURI().toURL());
            }
            return new ClassLoader[]{
                    new URLClassLoader(compileJars.toArray(new URL[compileJars.size()]),
                            MavenGeneratorManager.class.getClassLoader())
            };

        } catch (Exception e) {
            projectContext.getLogger().warn("Instructed to use project classpath, but cannot. Continuing build if we can: ", e);
            return new ClassLoader[0];
        }
    }
}