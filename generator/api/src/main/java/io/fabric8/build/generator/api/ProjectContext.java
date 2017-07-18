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

package io.fabric8.build.generator.api;

import java.util.Properties;

import io.fabric8.build.common.BuildLogger;


/**
 * @author nicola
 * @since 17.07.17
 */
public class ProjectContext {

    // project props
    private Properties properties;

    private BuildLogger logger;

    private boolean prePackagePhase;

    private FromSelector fromSelector;

    public ProjectContext(Properties properties, BuildLogger logger, boolean prePackagePhase, FromSelector fromSelector) {
        this.properties = properties;
        this.logger = logger;
        this.prePackagePhase = prePackagePhase;
        this.fromSelector = fromSelector;
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
}
