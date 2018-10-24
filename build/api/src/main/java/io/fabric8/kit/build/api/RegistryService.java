package io.fabric8.kit.build.api;

import java.io.IOException;

import io.fabric8.kit.config.image.ImageConfiguration;
import io.fabric8.kit.config.image.build.ImagePullPolicy;


/**
 * @author roland
 * @since 17.10.18
 */
public interface RegistryService {

    void pushImage(ImageConfiguration imageConfig, int retries, boolean skipTag, RegistryContext registryContext) throws IOException;

    void pullImage(String image, ImagePullPolicy policy, RegistryContext registryContext) throws IOException;
}
