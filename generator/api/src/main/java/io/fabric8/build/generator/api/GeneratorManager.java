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

import io.fabric8.build.common.BuildLogger;
import io.fabric8.build.config.image.ImageConfiguration;


/**
 * Manager responsible for finding and calling generators
 * @author nicola
 * @since 17/07/17
 */
public class GeneratorManager<P extends ProjectContext> {


    private final List<Generator<P>> generators;
    private final P projectContext;
    private final GeneratorsConfig<P> generatorsConfig;
    private final BuildLogger log;

    public GeneratorManager(P projectContext,
                           GeneratorsConfig<P> generatorsConfig,
                           String[] generatorResources,
                           ClassLoader... extraClassLoaders) {
        GeneratorFactory<P> factory = new GeneratorFactory<>(extraClassLoaders);
        this.generators = factory.createGenerators(generatorResources);
        this.projectContext = projectContext;
        this.generatorsConfig = generatorsConfig;
        this.log = projectContext.getLogger();
    }


    public List<ImageConfiguration> process(Platform platform, List<ImageConfiguration> imageConfigs) {
        log.verbose("Generators:");
        for (Generator<P> generator : generators) {
            log.verbose(" - %s", generator.getName());
            GeneratorContext<P> context = createGeneratorContext(generator);
            if (generator.isApplicable(platform, context, imageConfigs)) {
                log.info("Running generator %s", generator.getName());
                generator.generate(platform, context, imageConfigs);
            }
        }
        return imageConfigs;
    }

    private GeneratorContext<P> createGeneratorContext(Generator<P> generator) {
        return generatorsConfig.createGeneratorContext(projectContext, generator.getName());
    }

}
