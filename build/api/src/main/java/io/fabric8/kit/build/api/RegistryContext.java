package io.fabric8.kit.build.api;

import java.io.IOException;

import io.fabric8.kit.build.api.auth.RegistryAuth;
import io.fabric8.kit.build.api.auth.RegistryAuthConfig;
import io.fabric8.kit.config.image.build.ImagePullPolicy;

/**
 * @author roland
 * @since 17.10.18
 */
public interface RegistryContext {

    ImagePullPolicy getDefaultImagePullPolicy();

    String getRegistry(RegistryAuthConfig.Kind kind);

    RegistryAuth getAuthConfig(RegistryAuthConfig.Kind kind, String user, String registry) throws IOException;

}
