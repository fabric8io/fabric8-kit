package io.fabric8.kit.build.service.docker.auth;

import java.io.IOException;

import io.fabric8.kit.build.api.auth.RegistryAuth;
import io.fabric8.kit.build.api.auth.RegistryAuthHandler;
import io.fabric8.kit.build.service.docker.auth.ecr.EcrExtendedAuth;
import io.fabric8.kit.common.KitLogger;

/**
 * @author roland
 * @since 21.10.18
 */
public class EcrExtendedRegistryAuthHandler implements RegistryAuthHandler.Extender {

    private final KitLogger log;

    public EcrExtendedRegistryAuthHandler(KitLogger log) {
        this.log = log;
    }

    @Override
    public String getId() {
        return "ecr";
    }

    public RegistryAuth extend(RegistryAuth given, String registry) throws IOException {
        EcrExtendedAuth ecr = new EcrExtendedAuth(log, registry);
        if (ecr.isAwsRegistry()) {
            return ecr.extendedAuth(given);
        }
        return given;
    }
}
