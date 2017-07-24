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

import java.util.List;

import io.fabric8.build.config.image.ImageConfiguration;


/**
 * Generator which can create {@link ImageConfiguration} on the fly by examining the project model.
 *
 * @author nicola
 * @since 17/07/17
 */
public interface Generator<P extends ProjectContext> {

    /**
     * The name of the generator
     * @return unique generator name
     */
    String getName();

    /**
     * Check whether this generator should kick in. The check must not examine anything
     * in the build directory (e.g. below `target/`) as data cannot be available
     * when this method is called in a pre package phase.
     *
     * @param platform the target platform
     * @param context the generator context
     * @param configs all configuration already available
     * @return true if the generator is applicable
     */
    boolean isApplicable(Platform platform, GeneratorContext<P> context, List<ImageConfiguration> configs);

    /**
     * Provide additional image configurations.
     *
     * @param platform the target platform
     * @param context the generator context
     * @param configs the already detected and resolved configuration
     */
    void generate(Platform platform, GeneratorContext<P> context, List<ImageConfiguration> configs);

}





