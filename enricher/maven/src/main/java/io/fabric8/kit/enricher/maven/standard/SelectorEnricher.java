package io.fabric8.kit.enricher.maven.standard;

import io.fabric8.kit.enricher.api.EnricherContext;
import io.fabric8.kit.enricher.api.Platform;
import io.fabric8.kit.enricher.maven.MavenBaseEnricher;
import io.fabric8.kit.enricher.maven.MavenProjectContext;
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
