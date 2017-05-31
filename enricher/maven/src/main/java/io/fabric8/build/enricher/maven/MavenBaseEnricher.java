package io.fabric8.build.enricher.maven;

import java.util.List;

import io.fabric8.build.enricher.api.Enricher;
import io.fabric8.build.enricher.api.EnricherContext;
import io.fabric8.build.enricher.api.Platform;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;

/**
 * @author roland
 * @since 31.05.17
 */
public class MavenBaseEnricher implements Enricher<MavenProjectContext> {

    private String name;

    protected MavenBaseEnricher(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<HasMetadata> convert(Platform platform, EnricherContext<MavenProjectContext> context, List<HasMetadata> items) {
        return null;
    }

    @Override
    public void create(Platform platform, EnricherContext<MavenProjectContext> context, KubernetesListBuilder builder) {

    }

    @Override
    public void enrich(Platform platform, EnricherContext<MavenProjectContext> context, KubernetesListBuilder builder) {

    }
}
