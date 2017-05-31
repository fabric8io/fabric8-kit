package io.fabric8.build.config.image;


public enum HealthCheckMode {

    /**
     * Mainly used to disable any health check provided by the base image.
     */
    none,

    /**
     * A command based health check.
     */
    cmd;

}
