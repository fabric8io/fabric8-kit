package io.fabric8.build.generator.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric8.build.config.image.BuildImageConfiguration;
import io.fabric8.build.config.image.ImageConfiguration;
import io.fabric8.build.config.image.ImageName;
import io.fabric8.build.generator.api.utils.Configs;

import org.apache.commons.lang3.StringUtils;

/**
 * @author nicola
 * @since 17.07.17
 */
public abstract class BaseGenerator<P extends ProjectContext> implements Generator<P> {

    private String name;

    protected BaseGenerator(String name) {
        this.name = name;
    }

    private enum Config implements Configs.Key {
        // The image name
        name,

        // The alias to use (default to the generator name)
        alias,

        // whether the generator should always add to already existing image configurationws
        add {{d = "false"; }},

        // Base image
        from,

        // Base image mode (only relevant for OpenShift)
        fromMode;

        public String def() { return d; } protected String d;
    }


    @Override
    public String getName() {
        return name;
    }

    protected String getConfig(GeneratorContext<P> context, Configs.Key key) {
        return getConfig(context, key, key.def());
    }

    protected String getConfig(GeneratorContext<P> context, Configs.Key key, String defaultVal) {
        String value = context.getConfig().get(key.name());
        if (value == null) {
            return defaultVal;
        }
        return value;
    }

    // Get 'from' as configured without any default and image stream tag handling
    protected String getFromAsConfigured(GeneratorContext<P> context) {
        return getConfigWithSystemFallbackAndDefault(context, Config.from, "fabric8.generator.from", null);
    }

    /**
     * Add the base image either from configuration or from a given selector
     *
     * @param builder for the build image configuration to add the from to.
     */
    protected void addFrom(Platform platform, GeneratorContext<P> context, String imageType, BuildImageConfiguration.Builder builder) {
        String fromMode = getConfigWithSystemFallbackAndDefault(context, Config.fromMode, "fabric8.generator.fromMode", getFromModeDefault(platform, context, imageType));
        String from = getConfigWithSystemFallbackAndDefault(context, Config.from, "fabric8.generator.from", null);
        FromSelector fromSelector = context.getProjectContext().getFromSelector();
        if ("docker".equalsIgnoreCase(fromMode)) {
            String fromImage = from;
            if (fromImage == null) {
                fromImage = fromSelector != null ? fromSelector.getFrom(platform, context, imageType) : null;
            }
            builder.from(fromImage);
            context.getLogger().info("Using Docker image %s as base / builder", fromImage);
        } else if ("istag".equalsIgnoreCase(fromMode)) {
            Map<String, String> fromExt = new HashMap<>();
            if (from != null) {
                ImageName iName = new ImageName(from);
                // user/project is considered to be the namespace
                String tag = iName.getTag();
                if (StringUtils.isBlank(tag)) {
                    tag = "latest";
                }
                fromExt.put(Platform.SourceStrategy.name.key(), iName.getSimpleName() + ":" + tag);
                if (iName.getUser() != null) {
                    fromExt.put(Platform.SourceStrategy.namespace.key(), iName.getUser());
                }
                fromExt.put(Platform.SourceStrategy.kind.key(), "ImageStreamTag");
            } else {
                fromExt = fromSelector != null ? fromSelector.getImageStreamTagFromExt(platform, context, imageType) : null;
            }
            if (fromExt != null) {
                String namespace = fromExt.get(Platform.SourceStrategy.namespace.key());
                if (namespace != null) {
                    context.getLogger().info("Using ImageStreamTag '%s' from namespace '%s' as builder image",
                            fromExt.get(Platform.SourceStrategy.name.key()), namespace);
                } else {
                    context.getLogger().info("Using ImageStreamTag '%s' as builder image",
                            fromExt.get(Platform.SourceStrategy.name.key()));
                }
                builder.fromExt(fromExt);
            }
        } else {
            throw new IllegalArgumentException(String.format("Invalid 'fromMode' in generator configuration for '%s'", getName()));
        }
    }

    // Use "istag" as default for "redhat" versions of this plugin
    private String getFromModeDefault(Platform platform, GeneratorContext<P> context, String imageType) {
        FromSelector fromSelector = context.getProjectContext().getFromSelector();
        if (fromSelector != null && fromSelector.useImageStreamTag(platform, context, imageType)) {
            return "istag";
        } else {
            return "docker";
        }
    }

    /**
     * Get Image name with a standard default
     *
     * @return Docker image name which is never null
     */
    protected String getImageName(Platform platform, GeneratorContext<P> context) {
        return getConfigWithSystemFallbackAndDefault(context, Config.name, "fabric8.generator.name", getDefaultImageUser(platform));
    }

    private String getDefaultImageUser(Platform platform) {
        if (platform == Platform.OPENSHIFT) {
            return "%a:%l";
        } else {
            return "%g/%a:%t";
        }
    }

    /**
     * Get alias name with the generator name as default
     * @return an alias which is never null;
     */
    protected String getAlias(Platform platform, GeneratorContext<P> context) {
        return getConfigWithSystemFallbackAndDefault(context, Config.alias, "fabric8.generator.alias", getName());
    }

    protected boolean shouldAddImageConfiguration(Platform platform, GeneratorContext<P> context, List<ImageConfiguration> configs) {
        return !containsBuildConfiguration(configs) || Configs.asBoolean(getConfig(context, Config.add));
    }

    protected String getConfigWithSystemFallbackAndDefault(GeneratorContext<P> context, Config name, String key, String defaultVal) {
        String value = getConfig(context, name);
        if (value == null) {
            value = Configs.getPropertyWithSystemAsFallback(context.getProjectContext().getProperties(), key);
        }
        return value != null ? value : defaultVal;
    }

    protected void addLatestTagIfSnapshot(GeneratorContext<P> context, BuildImageConfiguration.Builder buildBuilder) {
        if (context.getProjectContext().isSnapshot()) {
            buildBuilder.tags(Collections.singletonList("latest"));
        }
    }

    private boolean containsBuildConfiguration(List<ImageConfiguration> configs) {
        for (ImageConfiguration config : configs) {
            if (config.getBuildConfiguration() != null) {
                return true;
            }
        }
        return false;
    }


}
