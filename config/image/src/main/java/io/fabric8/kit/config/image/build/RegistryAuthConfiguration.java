package io.fabric8.kit.config.image.build;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class RegistryAuthConfiguration implements Serializable {

    private Map<String, String> push;

    private Map<String, String> pull;

    private String username;

    private String password;

    private String email;

    private String authToken;

    public Map toMap() {
        final Map authMap = new TreeMap<>();

        if (push != null) {
            authMap.put("push", push);
        }
        if (pull != null) {
            authMap.put("pull", pull);
        }

        String content[] = new String[] {
            "username", username,
            "password", password,
            "authToken", authToken,
            "email", email
        };
        for (int i = 0; i < content.length; i += 2) {
            if (content[i + 1] != null && content[i + 1].length() > 0) {
                authMap.put(content[i], content[i+1]);
            }
        }
        return authMap;
    }

}
