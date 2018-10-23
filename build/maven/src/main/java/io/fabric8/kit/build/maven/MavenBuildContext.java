package io.fabric8.kit.build.maven;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.fabric8.kit.build.api.BuildContext;
import io.fabric8.kit.build.api.RegistryContext;
import io.fabric8.kit.build.maven.assembly.DockerAssemblyConfigurationSource;
import io.fabric8.kit.common.KitLogger;
import io.fabric8.kit.config.image.build.BuildConfiguration;
import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.assembly.interpolation.AssemblyInterpolator;
import org.apache.maven.plugins.assembly.io.DefaultAssemblyReader;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.shared.filtering.MavenReaderFilter;
import org.codehaus.plexus.interpolation.fixed.FixedStringSearchInterpolator;

/**
 * @author roland
 * @since 16.10.18
 */
public class MavenBuildContext implements BuildContext {

    private String sourceDirectory;
    private String outputDirectory;
    private MavenProject project;
    private MavenSession session;
    private MavenFileFilter mavenFileFilter;
    private MavenReaderFilter mavenReaderFilter;
    private Settings settings;
    private List<MavenProject> reactorProjects;
    private MavenArchiveConfiguration archiveConfiguration;
    private MavenArchiveService archiveService;
    private RegistryContext registryContext;

    private MavenBuildContext() { }


    public String getSourceDirectory() {
        return sourceDirectory;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }


    public File getBasedir() {
        return project.getBasedir();
    }

    @Override
    public Properties getProperties() {
        return project.getProperties();
    }

    @Override
    public Function<String, String> createInterpolator(String filter) {
        FixedStringSearchInterpolator interpolator = createMavenInterpolator(this, filter);
        return interpolator::interpolate;
    }

    @Override
    public File createImageContentArchive(String imageName, BuildConfiguration buildConfig, KitLogger log) throws IOException {
        return archiveService.createArchive(imageName, buildConfig, this, log);
    }

    @Override
    public RegistryContext getRegistryContext() {
        return registryContext;
    }

    // =======================================================================================
    // Maven specific method not available via interface

    public MavenProject getProject() {
        return project;
    }

    public MavenSession getSession() {
        return session;
    }

    public MavenFileFilter getMavenFileFilter() {
        return mavenFileFilter;
    }

    public MavenReaderFilter getMavenReaderFilter() {
        return mavenReaderFilter;
    }

    public Settings getSettings() {
        return settings;
    }

	public List<MavenProject> getReactorProjects() {
		return reactorProjects;
	}

    public MavenArchiveConfiguration getArchiveConfiguration() {
        return archiveConfiguration;
    }

    // =======================================================================================

    /**
     * Create an interpolator for the given maven parameters and filter configuration.
     *
     * @param ctx The maven parameters.
     * @param filter The filter configuration.
     * @return An interpolator for replacing maven properties.
     */
    private FixedStringSearchInterpolator createMavenInterpolator(MavenBuildContext ctx, String filter) {
        String[] delimiters = extractDelimiters(filter);
        if (delimiters == null) {
            // Don't interpolate anything
            return FixedStringSearchInterpolator.create();
        }

        DockerAssemblyConfigurationSource configSource = new DockerAssemblyConfigurationSource(ctx, null, null);
        // Patterned after org.apache.maven.plugins.assembly.interpolation.AssemblyExpressionEvaluator
        return AssemblyInterpolator
                .fullInterpolator(ctx.getProject(),
                                  DefaultAssemblyReader.createProjectInterpolator(ctx.getProject())
                                                       .withExpressionMarkers(delimiters[0], delimiters[1]), configSource)
                .withExpressionMarkers(delimiters[0], delimiters[1]);
    }

    private static String[] extractDelimiters(String filter) {
        if (filter == null) {
            // Default interpolation scheme
            return new String[] { "${", "}" };
        }
        if (filter.equalsIgnoreCase("false") ||
            filter.equalsIgnoreCase("none")) {
            return null;
        }
        if (filter.contains("*")) {
            Matcher matcher = Pattern.compile("^(?<start>[^*]+)\\*(?<end>.*)$").matcher(filter);
            if (matcher.matches()) {
                return new String[] { matcher.group("start"), matcher.group("end") };
            }
        }
        return new String[] { filter, filter };
    }


    // =======================================================================================

    public static class Builder {

        private MavenBuildContext context;

        public Builder() {
            this.context = new MavenBuildContext();
        }

        public Builder(MavenBuildContext context) {
            this.context = context;
        }

        public Builder sourceDirectory(String sourceDirectory) {
            context.sourceDirectory = sourceDirectory;
            return this;
        }

        public Builder outputDirectory(String outputDirectory) {
            context.outputDirectory = outputDirectory;
            return this;
        }

        public Builder registryContext(RegistryContext registryContext) {
            context.registryContext = registryContext;
            return this;
        }

        // ===============================================================================
        // Maven specific calls

        public Builder project(MavenProject project) {
            context.project = project;
            return this;
        }

        public Builder session(MavenSession session) {
            context.session = session;
            return this;
        }

        public Builder settings(Settings settings) {
            context.settings = settings;
            return this;
        }

        public Builder mavenReaderFilter(MavenReaderFilter mavenReaderFilter) {
            context.mavenReaderFilter = mavenReaderFilter;
            return this;
        }

        public Builder mavenFileFilter(MavenFileFilter mavenFileFilter) {
            context.mavenFileFilter = mavenFileFilter;
            return this;
        }

        public Builder reactorProjects(List<MavenProject> reactorProjects) {
            context.reactorProjects = reactorProjects;
            return this;
        }

        public Builder archiveConfiguration(MavenArchiveConfiguration archiveConfiguration) {
            context.archiveConfiguration = archiveConfiguration;
            return this;
        }

        public Builder archiveService(MavenArchiveService archiveService) {
            context.archiveService = archiveService;
            return this;
        }

        // ================================================================================
        public MavenBuildContext build() {
            return context;
        }

    }
}
