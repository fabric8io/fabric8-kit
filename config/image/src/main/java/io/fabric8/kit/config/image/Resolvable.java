package io.fabric8.kit.config.image;

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
