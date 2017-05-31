package io.fabric8.build.config.image;

import java.util.List;

/**
 * @author roland
 * @since 31.05.17
 */
public interface Resolvable {
    String getName();
    String getAlias();
    List<String> getDependencies();
}
