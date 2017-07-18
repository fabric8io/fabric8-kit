package io.fabric8.build.generator.maven;

import java.util.List;

import io.fabric8.build.config.image.ImageConfiguration;
import io.fabric8.build.generator.api.Generator;
import io.fabric8.build.generator.api.GeneratorContext;

/**
 * @author roland
 * @since 17.07.17
 */
public class MavenBaseGenerator implements Generator<MavenProjectContext> {

    private String name;

    protected MavenBaseGenerator(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }


    @Override
    public boolean isApplicable(GeneratorContext<MavenProjectContext> context, List<ImageConfiguration> configs) {
        return false;
    }

    @Override
    public void generate(GeneratorContext<MavenProjectContext> context, List<ImageConfiguration> configs) {

    }

}
