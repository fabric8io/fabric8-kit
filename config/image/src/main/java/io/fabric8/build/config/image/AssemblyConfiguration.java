package io.fabric8.build.config.image;


import java.io.Serializable;

import io.fabric8.build.config.image.assembly.Assembly;

public class AssemblyConfiguration implements Serializable {

    /**
     * @deprecated Use 'targetDir' instead
     */
    @Deprecated
    private String basedir;

    /**
     * New replacement for base directory which better reflects its
     * purpose
     */

    private String targetDir;

    /**
     * Name of the assembly which is used also as name of the archive
     * which is created and has to be used when providing an own Dockerfile
     */
    private String name = "deployment";


    private String descriptor;

    private String descriptorRef;

    private Assembly inline;

    /**
     * @deprecated Use {@link BuildImageConfiguration#dockerFileDir} instead
     */

    @Deprecated
    private String dockerFileDir;

    // use 'exportTargetDir' instead
    @Deprecated
    private Boolean exportBasedir;

    /**
     * Whether the target directory should be
     * exported.
     *
     */

    private Boolean exportTargetDir;

    /**
     * @deprecated use permissionMode == ignore instead.
     */

    private Boolean ignorePermissions;


    private AssemblyMode mode;


    private String user;


    private String tarLongFileMode;

    public Boolean exportTargetDir() {
        if (exportTargetDir != null) {
            return exportTargetDir;
        } else if (exportBasedir != null) {
            return exportBasedir;
        } else {
            return null;
        }
    }

    public String getTargetDir() {
        if (targetDir != null) {
            return targetDir;
        } else if (basedir != null) {
            return basedir;
        } else {
            return "/" + getName();
        }
    }

    /**
     * @parameter default-value="keep"
     */
    private PermissionMode permissions;

    public String getDescriptor() {
        return descriptor;
    }

    public String getDescriptorRef() {
        return descriptorRef;
    }

    public Assembly getInline() {
        return inline;
    }

    public String getDockerFileDir() {
        return dockerFileDir;
    }

    public String getUser() {
        return user;
    }

    public AssemblyMode getMode() {
        return mode != null ? mode : AssemblyMode.dir;
    }

    public String getTarLongFileMode() {
        return tarLongFileMode;
    }

    public Boolean isIgnorePermissions() {
        // New permission mode has precedence
        if (permissions != null) {
            return permissions == PermissionMode.ignore;
        }
        return (ignorePermissions != null) ? ignorePermissions : Boolean.FALSE;
    }

     public PermissionMode getPermissions() {
        return permissions != null ? permissions : PermissionMode.keep;
    }

    public String getName() {
        return name;
    }

    public static class Builder {

        private final AssemblyConfiguration config = new AssemblyConfiguration();
        private boolean isEmpty = true;

        public AssemblyConfiguration build() {
            return isEmpty ? null : config;
        }

        public Builder targetDir(String targetDir) {
            config.targetDir = set(targetDir);
            return this;
        }

        public Builder descriptor(String descriptorFile) {
            config.descriptor = set(descriptorFile);
            return this;
        }

        public Builder descriptorRef(String descriptorRef) {
            config.descriptorRef = set(descriptorRef);
            return this;
        }

        public Builder inline(Assembly inline) {
            config.inline = set(inline);
            return this;
        }

        public Builder dockerFileDir(String dockerFileDir) {
            config.dockerFileDir = set(dockerFileDir);
            return this;
        }

        public Builder exportBasedir(Boolean export) {
            config.exportBasedir = set(export);
            return this;
        }

        @Deprecated
        public Builder ignorePermissions(Boolean ignorePermissions) {
            config.ignorePermissions = set(ignorePermissions);
            return this;
        }

        public Builder permissions(String permissions) {
            if (permissions != null) {
                config.permissions = PermissionMode.valueOf(permissions.toLowerCase());
            }
            return this;
        }

        public Builder user(String user) {
            config.user = set(user);
            return this;
        }

        public Builder mode(String mode) {
            if (mode != null) {
                config.mode = AssemblyMode.valueOf(mode.toLowerCase());
                isEmpty = false;
            }
            return this;
        }

        public Builder tarLongFileMode(String tarLongFileMode) {
            config.tarLongFileMode = set(tarLongFileMode);
            return this;
        }

         private <T> T set(T prop) {
            if (prop != null) {
                isEmpty = false;
            }
            return prop;
        }
    }

    public enum PermissionMode {

        /**
         * Auto detect permission mode
         */
        auto,

        /**
         * Make everything executable
         */
        exec,

        /**
         * Leave all as it is
         */
        keep,

        /**
         * Ignore permission when using an assembly mode of "dir"
         */
        ignore
    }
}
