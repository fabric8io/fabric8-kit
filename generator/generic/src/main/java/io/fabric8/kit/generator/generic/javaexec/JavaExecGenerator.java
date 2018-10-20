/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.fabric8.kit.generator.generic.javaexec;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import io.fabric8.build.common.Fabric8ExecutionException;
import io.fabric8.kit.config.image.AssemblyConfiguration;
import io.fabric8.kit.config.image.BuildImageConfiguration;
import io.fabric8.kit.config.image.ImageConfiguration;
import io.fabric8.kit.config.image.assembly.Assembly;
import io.fabric8.kit.config.image.assembly.DependencySet;
import io.fabric8.kit.config.image.assembly.FileSet;
import io.fabric8.kit.generator.api.BaseGenerator;
import io.fabric8.kit.generator.api.GeneratorContext;
import io.fabric8.kit.generator.api.Platform;
import io.fabric8.kit.generator.api.ProjectContext;
import io.fabric8.kit.generator.api.utils.Configs;
import org.apache.commons.lang3.StringUtils;

/**
 * @author roland
 * @since 21/09/16
 */

public class JavaExecGenerator<P extends ProjectContext> extends BaseGenerator<P> {

    // Environment variable used for specifying a main class
    static final String JAVA_MAIN_CLASS_ENV_VAR = "JAVA_MAIN_CLASS";
    private static final String JAVA_OPTIONS = "JAVA_OPTIONS";

    private static final String IMAGE_TYPE = "java";

    // Plugins indicating a plain java build
    private static final String[] JAVA_EXEC_MAVEN_PLUGINS = new String[] {
        "org.codehaus.mojo:exec-maven-plugin",
        "org.apache.maven.plugins:maven-shade-plugin"
    };

    public JavaExecGenerator() {
        this("java-exec");
    }

    protected JavaExecGenerator(String name) {
        super(name);
    }

    public enum Config implements Configs.Key {
        // Webport to expose. Set to 0 if no port should be exposed
        webPort        {{ d = "8080"; }},

        // Jolokia from the base image to expose. Set to 0 if no such port should be exposed
        jolokiaPort    {{ d = "8778"; }},

        // Prometheus port from base image. Set to 0 if no required
        prometheusPort {{ d = "9779"; }},

        // Basedirectory where to put the application data into (within the Docker image
        targetDir {{d = "/deployments"; }},

        // The name of the main class for non-far jars. If not speficied it is tried
        // to find a main class within target/classes.
        mainClass,

        // Reference to a predefined assembly descriptor to use. By defult it is tried to be detected
        assemblyRef;

        public String def() { return d; } protected String d;
    }


    @Override
    public boolean isApplicable(Platform platform, GeneratorContext<P> context, List<ImageConfiguration> configs) {
        if (shouldAddImageConfiguration(platform, context, configs)) {
            // If a main class is configured, we always kick in
            if (getConfig(context, Config.mainClass) != null) {
                return true;
            }

            // TODO this maven stuff should be generalized?
//            // Check for the existing of plugins indicating a plain java exec app
//            for (String plugin : JAVA_EXEC_MAVEN_PLUGINS) {
//                if (MavenUtil.hasPlugin(getProject(), plugin)) {
//                    return true;
//                }
//            }
        }
        return false;
    }

    /**
     * Provide additional image configurations.
     *  @param platform the target platform
     * @param context the generator context

     * @param configs the already detected and resolved configuration
     */
    @Override
    public void generate(Platform platform, GeneratorContext<P> context, List<ImageConfiguration> configs) {
        try {
            ImageConfiguration.Builder imageBuilder = new ImageConfiguration.Builder();
            BuildImageConfiguration.Builder buildBuilder = null;
            buildBuilder = new BuildImageConfiguration.Builder()
                    .ports(extractPorts(context));
            addFrom(platform, context, IMAGE_TYPE, buildBuilder);
            if (!context.getProjectContext().isPrePackagePhase()) {
                // Only add assembly if not in a pre-package phase where the referenced files
                // won't be available.
                buildBuilder.assembly(createAssembly(context));
            }
            MainClassDetector mainClassDetector = new MainClassDetector(getConfig(context, Config.mainClass),
                    context.getProjectContext().getBuildOutputDirectory(),
                    context.getLogger());
            Map<String, String> envMap = getEnv(context, mainClassDetector, context.getProjectContext().isPrePackagePhase());
            envMap.put("JAVA_APP_DIR", getConfig(context, Config.targetDir));
            buildBuilder.env(envMap);
            addLatestTagIfSnapshot(context, buildBuilder);
            imageBuilder
                    .name(getImageName(platform, context))
                    .alias(getAlias(platform, context))
                    .buildConfig(buildBuilder.build());
            configs.add(imageBuilder.build());
        } catch (Fabric8ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Hook for adding extra environment vars
     *
     * @return map with environment variables to use
     * @param prePackagePhase
     */
    protected Map<String, String> getEnv(GeneratorContext<P> context, MainClassDetector mainClassDetector, boolean prePackagePhase) throws Fabric8ExecutionException {
        Map<String, String> ret = new HashMap<>();
        if (!isFatJar(context)) {
            String mainClass = getConfig(context, Config.mainClass);
            if (mainClass == null) {
                mainClass = mainClassDetector.getMainClass();
                if (mainClass == null) {
                    if (!prePackagePhase) {
                        throw new IllegalStateException("Cannot extract main class to startup");
                    }
                }
            }
            if (mainClass != null) {
                context.getLogger().verbose("Detected main class %s", mainClass);
                ret.put(JAVA_MAIN_CLASS_ENV_VAR, mainClass);
            }
        }
        List<String> javaOptions = getExtraJavaOptions();
        if (javaOptions.size() > 0) {
            ret.put(JAVA_OPTIONS, StringUtils.join(javaOptions.iterator()," "));
        }
        return ret;
    }

    protected List<String> getExtraJavaOptions() {
        return new ArrayList<>();
    }

    protected AssemblyConfiguration createAssembly(GeneratorContext<P> context) throws Fabric8ExecutionException {
        AssemblyConfiguration.Builder builder = new AssemblyConfiguration.Builder().targetDir(getConfig(context, Config.targetDir));
        addAssembly(context, builder);
        return builder.build();
    }

    protected void addAssembly(GeneratorContext<P> context, AssemblyConfiguration.Builder builder) throws Fabric8ExecutionException {
        String assemblyRef = getConfig(context, Config.assemblyRef);
        if (assemblyRef != null) {
            builder.descriptorRef(assemblyRef);
        } else {
            if (isFatJar(context)) {
                FatJarDetector.Result fatJar = detectFatJar(context);
                Assembly assembly = new Assembly();
                if (fatJar == null) {
                    DependencySet dependencySet = new DependencySet();
                    dependencySet.addInclude(context.getProjectContext().getGroupId() + ":" + context.getProjectContext().getArtifactId());
                    assembly.addDependencySet(dependencySet);
                } else {
                    FileSet fileSet = new FileSet();
                    File buildDir = context.getProjectContext().getBuildDirectory();
                    fileSet.setDirectory(toRelativePath(buildDir, context.getProjectContext().getBaseDirectory()));
                    fileSet.addInclude(toRelativePath(fatJar.getArchiveFile(), buildDir));
                    fileSet.setOutputDirectory(".");
                    assembly.addFileSet(fileSet);
                }
                assembly.addFileSet(createFileSet("src/main/fabric8-includes/bin","bin","0755","0755"));
                assembly.addFileSet(createFileSet("src/main/fabric8-includes",".","0644","0755"));
                builder.inline(assembly);
            } else {
                builder.descriptorRef("artifact-with-dependencies");
            }
        }
    }

    private String toRelativePath(File archiveFile, File basedir) {
        String absolutePath = archiveFile.getAbsolutePath();
        absolutePath = absolutePath.replace('\\', '/');
        String basedirPath = basedir.getAbsolutePath().replace('\\', '/');
        return absolutePath.startsWith(basedirPath) ?
            absolutePath.substring(basedirPath.length() + 1) :
            absolutePath;
    }

    private FileSet createFileSet(String sourceDir, String outputDir, String fileMode, String directoryMode) {
        FileSet fileSet = new FileSet();
        fileSet.setDirectory(sourceDir);
        fileSet.setOutputDirectory(outputDir);
        fileSet.setFileMode(fileMode);
        fileSet.setDirectoryMode(directoryMode);
        return fileSet;
    }

    protected boolean isFatJar(GeneratorContext<P> context) throws Fabric8ExecutionException {
        return !hasMainClass(context) && detectFatJar(context) != null;
    }

    protected boolean hasMainClass(GeneratorContext<P> context) {
        return getConfig(context, Config.mainClass) != null;
    }

    public FatJarDetector.Result detectFatJar(GeneratorContext<P> context) throws Fabric8ExecutionException {
        FatJarDetector fatJarDetector = new FatJarDetector(context.getProjectContext().getBuildDirectory());
        return fatJarDetector.scan();
    }

    protected List<String> extractPorts(GeneratorContext<P> context) {
        // TODO would rock to look at the base image and find the exposed ports!
        List<String> answer = new ArrayList<>();
        addPortIfValid(answer, getConfig(context, Config.webPort));
        addPortIfValid(answer, getConfig(context, Config.jolokiaPort));
        addPortIfValid(answer, getConfig(context, Config.prometheusPort));
        return answer;
    }

    protected void addPortIfValid(List<String> list, String port) {
        if (!Strings.isNullOrEmpty(port) && Integer.parseInt(port) > 0) {
            list.add(port);
        }
    }
}
