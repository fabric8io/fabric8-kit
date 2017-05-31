package io.fabric8.build.enricher.maven.standard;

import io.fabric8.build.enricher.api.EnricherContext;
import io.fabric8.build.enricher.api.Platform;
import io.fabric8.build.enricher.maven.MavenBaseEnricher;
import io.fabric8.build.enricher.maven.MavenProjectContext;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;

/**
 * @author roland
 * @since 31.05.17
 */
public class SelectorEnricher extends MavenBaseEnricher {

    protected SelectorEnricher() {
        super("fmp-selector");
    }

    @Override
    public void enrich(Platform platform, EnricherContext<MavenProjectContext> context, KubernetesListBuilder builder) {
        super.enrich(platform, context, builder);
    }
}
