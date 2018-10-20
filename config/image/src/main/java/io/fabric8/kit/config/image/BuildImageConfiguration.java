package io.fabric8.kit.config.image;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import io.fabric8.kit.common.KitLogger;
import io.fabric8.kit.config.image.util.DeepCopy;

/**
 * @author roland
 * @since 02.09.14
 */
public class BuildImageConfiguration implements Serializable {

    public static final String DEFAULT_FILTER = "${*}";
    public static final String DEFAULT_CLEANUP = "try";

    /**
     * Directory holding an external Dockerfile which is used to build the
     * image. This Dockerfile will be enriched by the addition build configuration
     */
    private String dockerFileDir;

    /**
     * Path to a dockerfile to use. Its parent directory is used as build context (i.e. as <code>dockerFileDir</code>).
     * Multiple different Dockerfiles can be specified that way. If set overwrites a possibly givem
     * <code>dockerFileDir</code>
     */
    private String dockerFile;

    /**
     * Path to a docker archive to load an image instead of building from scratch.
     * Note only either dockerFile/dockerFileDir or
     * dockerArchive can be used.
     */
    private String dockerArchive;

    /**
     * How interpolation of a dockerfile should be performed
     */
    private String filter = DEFAULT_FILTER;

    /**
     * Base Image
     */
    private String from;

    /**
     * Extended version for <from>
     */
    private Map<String, String> fromExt;

    private String registry;

    private String maintainer;

    private List<String> ports;

    /**
     * RUN Commands within Build/Image
     */
    private List<String> runCmds;

    private String cleanup = DEFAULT_CLEANUP;

    private boolean nocache = false;

    private boolean optimise = false;

    private List<String> volumes;

    private List<String> tags;

    private Map<String, String> env;

    private Map<String, String> labels;

    private Map<String, String> args;

    private Arguments entryPoint;

    @Deprecated
    private String command;

    private String workdir;

    private Arguments cmd;

    private String user;

    private HealthCheckConfiguration healthCheck;

    private AssemblyConfiguration assembly;

    private boolean skip = false;

    private ArchiveCompression compression = ArchiveCompression.none;

    private Map<String,String> buildOptions;

    // Path to Dockerfile to use, initialized lazily ....
    private File dockerFileFile, dockerArchiveFile;

    public BuildImageConfiguration() {}

    public boolean isDockerFileMode() {
        return dockerFileFile != null;
    }

    public File getDockerFile() {
        return dockerFileFile;
    }

    public File getDockerArchive() {
        return dockerArchiveFile;
    }

    public String getFilter() {
        return filter;
    }

    public String getFrom() {
        if (from == null && getFromExt() != null) {
            return getFromExt().get("name");
        }
        return from;
    }

    public Map<String, String> getFromExt() {
        return fromExt;
    }

    public String getRegistry() {
        return registry;
    }

    public String getMaintainer() {
        return maintainer;
    }

    public String getWorkdir() {
        return workdir;
    }

    public AssemblyConfiguration getAssemblyConfiguration() {
        return assembly;
    }

    public List<String> getPorts() {
        return ports;
    }

    public List<String> getVolumes() {
        return volumes != null ? volumes : Collections.<String>emptyList();
    }

    public List<String> getTags() {
        return tags != null ? tags : Collections.<String>emptyList();
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public Arguments getCmd() {
        return cmd;
    }

    @Deprecated
    public String getCommand() {
        return command;
    }

    public CleanupMode cleanupMode() {
        return CleanupMode.parse(cleanup);
    }

    public boolean nocache() {
        return nocache;
    }

    public boolean optimise() {
        return optimise;
    }

    public boolean skip() {
        return skip;
    }

    public ArchiveCompression getCompression() {
        return compression;
    }

    public Map<String, String> getBuildOptions() {
        return buildOptions;
    }

    public Arguments getEntryPoint() {
        return entryPoint;
    }

    public List<String> getRunCmds() {
        return runCmds;
    }

    public String getUser() {
      return user;
    }

    public HealthCheckConfiguration getHealthCheck() {
        return healthCheck;
    }

    public Map<String, String> getArgs() {
        return args;
    }

    public static class Builder {
        private final BuildImageConfiguration config;

        public Builder() {
            this(null);
        }

        public Builder(BuildImageConfiguration that) {
            if (that == null) {
                this.config = new BuildImageConfiguration();
            } else {
                this.config = DeepCopy.copy(that);
            }
        }

        public Builder dockerFileDir(String dir) {
            config.dockerFileDir = dir;
            return this;
        }

        public Builder dockerFile(String file) {
            config.dockerFile = file;
            return this;
        }

        public Builder dockerArchive(String archive) {
            config.dockerArchive = archive;
            return this;
        }

        public Builder filter(String filter) {
            if (filter == null) {
                config.filter = DEFAULT_FILTER;
            } else {
                config.filter = filter;
            }
            return this;
        }

        public Builder from(String from) {
            config.from = from;
            return this;
        }

        public Builder fromExt(Map<String, String> fromExt) {
            config.fromExt = fromExt;
            return this;
        }

        public Builder registry(String registry) {
            config.registry = registry;
            return this;
        }

        public Builder maintainer(String maintainer) {
            config.maintainer = maintainer;
            return this;
        }

        public Builder workdir(String workdir) {
            config.workdir = workdir;
            return this;
        }

        public Builder assembly(AssemblyConfiguration assembly) {
            config.assembly = assembly;
            return this;
        }

        public Builder ports(List<String> ports) {
            config.ports = ports;
            return this;
        }

        public Builder runCmds(List<String> theCmds) {
            if (theCmds == null) {
                config.runCmds = new ArrayList<>();
            } else {
                config.runCmds = theCmds;
            }
            return this;
        }

        public Builder volumes(List<String> volumes) {
            config.volumes = volumes;
            return this;
        }

        public Builder tags(List<String> tags) {
            config.tags = tags;
            return this;
        }

        public Builder env(Map<String, String> env) {
            config.env = env;
            return this;
        }

        public Builder args(Map<String, String> args) {
            config.args = args;
            return this;
        }

        public Builder labels(Map<String, String> labels) {
            config.labels = labels;
            return this;
        }

        public Builder cmd(String cmd) {
            if (cmd != null) {
                config.cmd = new Arguments(cmd);
            }
            return this;
        }

        public Builder cleanup(String cleanup) {
            if (cleanup == null) {
                config.cleanup = DEFAULT_CLEANUP;
            } else {
                config.cleanup = cleanup;
            }
            return this;
        }

        public Builder compression(String compression) {
            if (compression == null) {
                config.compression = ArchiveCompression.none;
            } else {
                config.compression = ArchiveCompression.valueOf(compression);
            }
            return this;
        }

        public Builder nocache(String nocache) {
            if (nocache != null) {
                config.nocache = Boolean.valueOf(nocache);
            }
            return this;
        }

        public Builder optimise(String optimise) {
            if (optimise != null) {
                config.optimise = Boolean.valueOf(optimise);
            }
            return this;
        }

        public Builder entryPoint(String entryPoint) {
            if (entryPoint != null) {
                config.entryPoint = new Arguments(entryPoint);
            }
            return this;
        }

        public Builder user(String user) {
            config.user = user;
            return this;
        }

        public Builder healthCheck(HealthCheckConfiguration healthCheck) {
            config.healthCheck = healthCheck;
            return this;
        }

        public Builder skip(String skip) {
            if (skip != null) {
                config.skip = Boolean.valueOf(skip);
            }
            return this;
        }

        public Builder buildOptions(Map<String,String> buildOptions) {
            config.buildOptions = buildOptions;
            return this;
        }

        public BuildImageConfiguration build() {
            return config;
        }
    }

    public String initAndValidate(KitLogger log) throws IllegalArgumentException {
        if (entryPoint != null) {
            entryPoint.validate();
        }
        if (cmd != null) {
            cmd.validate();
        }
        if (healthCheck != null) {
            healthCheck.validate();
        }

        if (command != null) {
            log.warn("<command> in the <build> configuration is deprecated and will be be removed soon");
            log.warn("Please use <cmd> with nested <shell> or <exec> sections instead.");
            log.warn("");
            log.warn("More on this is explained in the user manual: ");
            log.warn("https://github.com/fabric8io/docker-maven-plugin/blob/master/doc/manual.md#start-up-arguments");
            log.warn("");
            log.warn("Migration is trivial, see changelog to version 0.12.0 -->");
            log.warn("https://github.com/fabric8io/docker-maven-plugin/blob/master/doc/changelog.md");
            log.warn("");
            log.warn("For now, the command is automatically translated for you to the shell form:");
            log.warn("   <cmd>%s</cmd>", command);
        }

        initDockerFileFile(log);

        if (healthCheck != null) {
            // HEALTHCHECK support added later
            return "1.24";
        } else if (args != null) {
            // ARG support came in later
            return "1.21";
        } else {
            return null;
        }
    }

    // Initialize the dockerfile location and the build mode
    private void initDockerFileFile(KitLogger log) {
        // can't have dockerFile/dockerFileDir and dockerArchive
        if ((dockerFile != null || dockerFileDir != null) && dockerArchive != null) {
            throw new IllegalArgumentException("Both <dockerFile> (<dockerFileDir>) and <dockerArchive> are set. " +
                                               "Only one of them can be specified.");
        }
        dockerFileFile = findDockerFileFile(log);

        if (dockerArchive != null) {
            dockerArchiveFile = new File(dockerArchive);
        }
    }

    private File findDockerFileFile(KitLogger log) {
        if (dockerFile != null) {
            File dFile = new File(dockerFile);
            if (dockerFileDir == null) {
                return dFile;
            } else {
                if (dFile.isAbsolute()) {
                    throw new IllegalArgumentException("<dockerFile> can not be absolute path if <dockerFileDir> also set.");
                }
                return new File(dockerFileDir, dockerFile);
            }
        }

        if (dockerFileDir != null) {
            return new File(dockerFileDir, "Dockerfile");
        }

        // TODO: Remove the following deprecated handling section
        if (dockerArchive == null) {
            String deprecatedDockerFileDir =
                getAssemblyConfiguration() != null ?
                    getAssemblyConfiguration().getDockerFileDir() :
                    null;
            if (deprecatedDockerFileDir != null) {
                log.warn("<dockerFileDir> in the <assembly> section of a <build> configuration is deprecated");
                log.warn("Please use <dockerFileDir> or <dockerFile> directly within the <build> configuration instead");
                return new File(deprecatedDockerFileDir,"Dockerfile");
            }
        }

        // No dockerfile mode
        return null;
    }
}
