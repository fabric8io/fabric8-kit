package io.fabric8.kit.build.maven.assembly;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import io.fabric8.kit.build.maven.MavenBuildContext;
import io.fabric8.kit.common.KitLogger;
import io.fabric8.kit.config.image.build.AssemblyConfiguration;
import io.fabric8.kit.config.image.build.BuildConfiguration;
import io.fabric8.kit.config.image.build.DockerFileBuilder;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.apache.maven.plugins.assembly.AssemblerConfigurationSource;
import org.apache.maven.plugins.assembly.InvalidAssemblerConfigurationException;
import org.apache.maven.plugins.assembly.archive.ArchiveCreationException;
import org.apache.maven.plugins.assembly.archive.AssemblyArchiver;
import org.apache.maven.plugins.assembly.format.AssemblyFormattingException;
import org.apache.maven.plugins.assembly.io.AssemblyReadException;
import org.apache.maven.plugins.assembly.io.AssemblyReader;
import org.apache.maven.plugins.assembly.model.Assembly;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.util.ReflectionUtils;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class DockerAssemblyManagerTest {

    @Tested
    private DockerAssemblyManager assemblyManager;

    @Injectable
    private AssemblyArchiver assemblyArchiver;

    @Injectable
    private AssemblyReader assemblyReader;

    @Injectable
    private ArchiverManager archiverManager;

    @Injectable
    private MappingTrackArchiver trackArchiver;

    @Test
    public void testNoAssembly() {
        BuildConfiguration buildConfig = new BuildConfiguration();
        AssemblyConfiguration assemblyConfig = buildConfig.getAssemblyConfiguration();

        DockerFileBuilder builder = assemblyManager.createDockerFileBuilder(buildConfig, assemblyConfig);
        String content = builder.content();

        assertFalse(content.contains("COPY"));
        assertFalse(content.contains("VOLUME"));
    }

    @Test
    public void assemblyFiles(@Injectable final MavenBuildContext mavenBuildContext,
                              @Injectable final MavenProject project,
                              @Injectable final Assembly assembly) throws AssemblyFormattingException, ArchiveCreationException, InvalidAssemblerConfigurationException, AssemblyReadException, IllegalAccessException, IOException {

        ReflectionUtils.setVariableValueInObject(assemblyManager, "trackArchiver", trackArchiver);

        new Expectations() {{
            mavenBuildContext.getOutputDirectory();
            result = "target/"; times = 3;

            mavenBuildContext.getBasedir();
            result = new File(".");

            assemblyReader.readAssemblies((AssemblerConfigurationSource) any);
            result = Arrays.asList(assembly);

        }};

        BuildConfiguration buildConfig = createBuildConfig();

        assemblyManager.getAssemblyFiles("testImage", buildConfig, mavenBuildContext, new KitLogger.StdoutLogger());
    }

    @Test
    public void testCopyValidVerifyGivenDockerfile(@Injectable final KitLogger logger) throws IOException {
        BuildConfiguration buildConfig = createBuildConfig();

        assemblyManager.verifyGivenDockerfile(
            new File(getClass().getResource("/docker/Dockerfile_assembly_verify_copy_valid.test").getPath()),
            buildConfig,
            s -> s,
            logger);

        new Verifications() {{
            logger.warn(anyString, (Object[]) any);times = 0;
        }};

    }

    @Test
    public void testCopyInvalidVerifyGivenDockerfile(@Injectable final KitLogger logger) throws IOException {
        BuildConfiguration buildConfig = createBuildConfig();

        assemblyManager.verifyGivenDockerfile(
            new File(getClass().getResource("/docker/Dockerfile_assembly_verify_copy_invalid.test").getPath()),
            buildConfig, s -> s,
            logger);

        new Verifications() {{
            logger.warn(anyString, (Object[]) any);times = 1;
        }};

    }

    @Test
    public void testCopyChownValidVerifyGivenDockerfile(@Injectable final KitLogger logger) throws IOException {
        BuildConfiguration buildConfig = createBuildConfig();

        assemblyManager.verifyGivenDockerfile(
            new File(getClass().getResource("/docker/Dockerfile_assembly_verify_copy_chown_valid.test").getPath()),
            buildConfig,
            s -> s,
            logger);

        new Verifications() {{
            logger.warn(anyString, (Object[]) any);times = 0;
        }};

    }

    private BuildConfiguration createBuildConfig() {
        return new BuildConfiguration.Builder()
                .assembly(new AssemblyConfiguration.Builder()
                        .descriptorRef("artifact")
                        .build())
                .build();
    }

}
