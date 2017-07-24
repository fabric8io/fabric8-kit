/*
 * Copyright 2016 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */

package io.fabric8.build.generator.maven;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import io.fabric8.build.generator.api.DefaultImageLookup;
import io.fabric8.build.generator.api.FromSelector;
import io.fabric8.build.generator.api.GeneratorContext;
import io.fabric8.build.generator.api.Platform;

import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;

import static io.fabric8.build.generator.api.Platform.SourceStrategy.kind;
import static io.fabric8.build.generator.api.Platform.SourceStrategy.name;
import static io.fabric8.build.generator.api.Platform.SourceStrategy.namespace;

/**
 * Helper class to encapsulate the selection of a base image
 *
 * @author roland
 * @since 12/08/16
 */
public abstract class MavenFromSelector implements FromSelector<MavenProjectContext> {

    private final Pattern REDHAT_VERSION_PATTERN = Pattern.compile("^.*\\.(redhat|fuse)-.*$");

    public MavenFromSelector() {
    }

    @Override
    public String getFrom(Platform platform, GeneratorContext<MavenProjectContext> context, String imageType) {
        if (platform == Platform.OPENSHIFT) {
            return getS2iBuildFrom(context, imageType);
        } else {
            return getDockerBuildFrom(context, imageType);
        }
    }

    @Override
    public Map<String, String> getImageStreamTagFromExt(Platform platform, GeneratorContext<MavenProjectContext> context, String imageType) {
        Map<String, String> ret = new HashMap<>();
        ret.put(kind.key(), "ImageStreamTag");
        ret.put(namespace.key(), "openshift");
        ret.put(name.key(), getIstagFrom(context, imageType));
        return ret;
    }

    @Override
    public boolean useImageStreamTag(Platform platform, GeneratorContext<MavenProjectContext> context, String imageType) {
        return platform == Platform.OPENSHIFT && isRedHat(context);
    }

    abstract protected String getDockerBuildFrom(GeneratorContext<MavenProjectContext> context, String prefix);

    abstract protected String getS2iBuildFrom(GeneratorContext<MavenProjectContext> context, String prefix);

    abstract protected String getIstagFrom(GeneratorContext<MavenProjectContext> context, String prefix);

    public boolean isRedHat(GeneratorContext<MavenProjectContext> context) {
        MavenProject project = context.getProjectContext().getProject();
        // TODO evaluate if it's worth adding the docker maven plugin or change strategy
        Plugin plugin = project.getPlugin("io.fabric8:fabric8-maven-plugin");
        if (plugin == null) {
            // Can happen if not configured in a build section but only in a dependency management section
            return false;
        }
        String version = plugin.getVersion();
        return REDHAT_VERSION_PATTERN.matcher(version).matches();
    }

    public static class Default extends MavenFromSelector {

        private final DefaultImageLookup lookup;

        public Default() {
            this.lookup = new DefaultImageLookup(Default.class);
        }

        @Override
        protected String getDockerBuildFrom(GeneratorContext<MavenProjectContext> context, String prefix) {
            return isRedHat(context) ? lookup.getImageName(prefix + ".redhat.docker") : lookup.getImageName(prefix + ".upstream.docker");
        }

        @Override
        protected String getS2iBuildFrom(GeneratorContext<MavenProjectContext> context, String prefix) {
            return isRedHat(context) ? lookup.getImageName(prefix + ".redhat.s2i") : lookup.getImageName(prefix + ".upstream.s2i");
        }

        protected String getIstagFrom(GeneratorContext<MavenProjectContext> context, String prefix) {
            return isRedHat(context) ? lookup.getImageName(prefix + ".redhat.istag") : lookup.getImageName(prefix + ".upstream.istag");
        }
    }
}
