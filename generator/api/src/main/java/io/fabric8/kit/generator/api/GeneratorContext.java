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

import java.util.Map;

import io.fabric8.kit.common.BuildLogger;

/**
 * @author nicola
 * @since 17.07.17
 */
public class GeneratorContext<P extends ProjectContext> {

    private final P projectContext;

    // Custom configuration for this generator
    private final Map<String, String> config;

    private final BuildLogger logger;

    /**
     * Get the configuration for this specific generator
     */
    public Map<String, String> getConfig() {
        return config;
    }

    public P getProjectContext() {
        return projectContext;
    }

    public BuildLogger getLogger() {
        return logger;
    }

    GeneratorContext(P projectContext, Map<String, String> config, BuildLogger logger) {
        this.projectContext = projectContext;
        this.config = config;
        this.logger = logger;
    }
}
