package io.fabric8.build.enricher.api;

import java.util.ArrayList;
import java.util.List;

import io.fabric8.build.common.BuildLogger;
import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;

/**
 * @author roland
 * @since 30.05.17
 */
public class EnricherManager<P extends ProjectContext> {

    private final List<Enricher<P>> enrichers;
    private final P projectContext;
    private final EnrichersConfig<P> enrichersConfig;
    private final BuildLogger log;

    public EnricherManager(P projectContext,
                           EnrichersConfig<P> enrichersConfig,
                           String[] enricherResources,
                           ClassLoader... extraClassLoaders) {
        EnricherFactory<P> factory = new EnricherFactory<>(extraClassLoaders);
        this.enrichers = factory.createEnrichers(enricherResources);
        this.projectContext = projectContext;
        this.enrichersConfig = enrichersConfig;
        this.log = projectContext.getLogger();
    }

    public KubernetesListBuilder process(final Platform platform, List<HasMetadata> items) {
        final KubernetesListBuilder listBuilder = init(items, platform);
        create(platform, listBuilder);
        enrich(platform, listBuilder);
        return listBuilder;
    }

    // ==============================================================================================

    private void enrich(final Platform platform, final KubernetesListBuilder listBuilder) {
        log.verbose("%s Enricher: Enrich resource objects", platform);
        loop(new Function<Enricher<P>, Void>() {
            @Override
            public Void apply(Enricher<P> enricher) {
                EnricherContext<P> ctx = createEnricherContext(enricher);
                enricher.enrich(platform, ctx, listBuilder);
                return null;
            }
        });
    }

    private void create(final Platform platform, final KubernetesListBuilder listBuilder) {
        log.verbose("%s Enricher: Create missing default resources", platform);
        loop(new Function<Enricher<P>, Void>() {
            @Override
            public Void apply(Enricher<P> enricher) {
                EnricherContext<P> ctx = createEnricherContext(enricher);
                enricher.create(platform, ctx, listBuilder);
                return null;
            }
        });
    }

    private KubernetesListBuilder init(List<HasMetadata> items, Platform platform) {
        log.verbose("%s Enricher: Init and cross convert resource items", platform);
        List<HasMetadata> platformItems = extractSupportedItems(platform, items);
        platformItems.addAll(convertUnsupportedItems(platform, items));
        return new KubernetesListBuilder().withItems(items);
    }

    private List<HasMetadata> convertUnsupportedItems(Platform platform, List<HasMetadata> items) {
        List<HasMetadata> unsupported = new ArrayList<>(extractUnsupportedItems(platform, items));
        List<HasMetadata> converted = new ArrayList<>();
        for (Enricher<P> enricher : enrichers) {
            List<HasMetadata> c = enricher.convert(platform, createEnricherContext(enricher), unsupported);
            if (c != null) {
                converted.addAll(c);
            }
        }
        return converted;
    }

    private EnricherContext<P> createEnricherContext(Enricher<P> enricher) {
        return enrichersConfig.createEnricherContext(projectContext, enricher.getName());
    }

    private List<HasMetadata> extractSupportedItems(Platform platform, List<HasMetadata> items) {
        List<HasMetadata> ret = new ArrayList<>();
        for (HasMetadata item : items) {
            if (platform.supportsKind(item.getKind())) {
                ret.add(item);
            }
        }
        return ret;
    }

    private List<HasMetadata> extractUnsupportedItems(Platform platform, List<HasMetadata> items) {
        List<HasMetadata> ret = new ArrayList<>();
        for (HasMetadata item : items) {
            if (!platform.supportsKind(item.getKind())) {
                ret.add(item);
            }
        }
        return ret;
    }

    private void loop(Function<Enricher<P>, Void> function) {
        for (Enricher<P> enricher : enrichersConfig.filterEnrichers(enrichers)) {
            function.apply(enricher);
        }
    }
}
