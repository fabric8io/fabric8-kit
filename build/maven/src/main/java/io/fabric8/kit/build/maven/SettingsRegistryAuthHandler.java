package io.fabric8.kit.build.maven;

import java.util.function.Function;

import io.fabric8.kit.build.api.auth.RegistryAuth;
import io.fabric8.kit.build.api.auth.RegistryAuthConfig;
import io.fabric8.kit.build.api.auth.RegistryAuthHandler;
import io.fabric8.kit.common.KitLogger;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.xml.Xpp3Dom;

/**
 * @author roland
 * @since 21.10.18
 */
public class SettingsRegistryAuthHandler implements RegistryAuthHandler {

    private static final String[] DEFAULT_REGISTRIES = new String[]{
        "docker.io", "index.docker.io", "registry.hub.docker.com"
    };

    private final Settings settings;
    private final KitLogger log;

    public SettingsRegistryAuthHandler(Settings settings, KitLogger log) {
        this.settings = settings;
        this.log = log;
    }

    @Override
    public String getId() {
        return "settings";
    }

    @Override
    public RegistryAuth create(RegistryAuthConfig.Kind kind, String user, String registry, Function<String, String> decryptor) {
        // Now lets lookup the registry & user from ~/.m2/setting.xml
        Server defaultServer = null;
        Server found;
        for (Server server : settings.getServers()) {
            String id = server.getId();

            // Remember a default server without user as fallback for later
            if (defaultServer == null) {
                defaultServer = checkForServer(server, id, registry, null);
            }
            // Check for specific server with user part
            found = checkForServer(server, id, registry, user);
            if (found != null) {
                log.debug("AuthConfig: credentials from ~/.m2/setting.xml (%s)", found);
                return createAuthConfigFromServer(found, decryptor);
            }
        }

        if (defaultServer != null) {
            log.debug("AuthConfig: credentials from ~/.m2/setting.xml (%s)", defaultServer);
            return createAuthConfigFromServer(defaultServer, decryptor);
        }

        return null;
    }

    private Server checkForServer(Server server, String id, String registry, String user) {

        String[] registries = registry != null ? new String[]{registry} : DEFAULT_REGISTRIES;
        for (String reg : registries) {
            if (id.equals(user == null ? reg : reg + "/" + user)) {
                return server;
            }
        }
        return null;
    }

    private RegistryAuth createAuthConfigFromServer(Server server, Function<String, String> decryptor) {
        return new RegistryAuth.Builder()
            .username(server.getUsername())
            .password(server.getPassword(), decryptor)
            .email(extractFromServerConfiguration(server.getConfiguration(), RegistryAuth.EMAIL))
            .auth(extractFromServerConfiguration(server.getConfiguration(), RegistryAuth.AUTH))
            .build();
    }

    private String extractFromServerConfiguration(Object configuration, String prop) {
        if (configuration != null) {
            Xpp3Dom dom = (Xpp3Dom) configuration;
            Xpp3Dom element = dom.getChild(prop);
            if (element != null) {
                return element.getValue();
            }
        }
        return null;
    }
}
