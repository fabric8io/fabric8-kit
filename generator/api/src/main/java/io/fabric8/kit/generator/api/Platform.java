package io.fabric8.kit.generator.api;

/**
 * @author nicola
 * @since 21.07.17
 */
public enum Platform {

    /**
     * Plain Docker platform, using Docker images.
     */
    DOCKER("Docker"),

    /**
     * Openshift platform, using ImageStreams and S2I build images.
     */
    OPENSHIFT("S2I");


    // Source strategy elemens
    public enum SourceStrategy {
        kind,
        namespace,
        name;

        public String key() {
            // Return the name, could be mapped if needed.
            return name();
        }
    }


    private final String label;

    private Platform(String label) {
        this.label = label;
    }

    /**
     * Check if the given type is same as the type stored in OpenShift
     *
     * @param type to check
     * @return
     */
    public boolean isSame(String type) {
        return type != null &&
                (type.equalsIgnoreCase("source") && this == OPENSHIFT) ||
                (type.equalsIgnoreCase("docker") && this == DOCKER);
    }

    public String getLabel() {
        return label;
    }


}
