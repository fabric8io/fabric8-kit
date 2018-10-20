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

package io.fabric8.kit.generator.api;

import java.io.File;
import java.util.Properties;

import io.fabric8.kit.common.BuildLogger;


/**
 * @author nicola
 * @since 17.07.17
 */
public class ProjectContext {

    // project props
    private final Properties properties;

    private final BuildLogger logger;

    private final boolean prePackagePhase;

    private final FromSelector fromSelector;

    private final boolean snapshot;

    private final File baseDirectory;

    private final File buildDirectory;

    private final File buildOutputDirectory;

    private final String groupId;

    private final String artifactId;

    private final String version;

    public ProjectContext(Properties properties, BuildLogger logger, boolean prePackagePhase, FromSelector fromSelector, boolean snapshot, File baseDirectory, File buildDirectory, File buildOutputDirectory, String groupId, String artifactId, String version) {
        this.properties = properties;
        this.logger = logger;
        this.prePackagePhase = prePackagePhase;
        this.fromSelector = fromSelector;
        this.snapshot = snapshot;
        this.baseDirectory = baseDirectory;
        this.buildDirectory = buildDirectory;
        this.buildOutputDirectory = buildOutputDirectory;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    /**
     * Get a project's properties
     *
     * @return project properties
     */
    public Properties getProperties() {
        return properties;
    }

    public BuildLogger getLogger() {
        return logger;
    }

    public boolean isPrePackagePhase() {
        return prePackagePhase;
    }

    public FromSelector getFromSelector() {
        return fromSelector;
    }

    public boolean isSnapshot() {
        return snapshot;
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    public File getBuildDirectory() {
        return buildDirectory;
    }

    public File getBuildOutputDirectory() {
        return buildOutputDirectory;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }
}
