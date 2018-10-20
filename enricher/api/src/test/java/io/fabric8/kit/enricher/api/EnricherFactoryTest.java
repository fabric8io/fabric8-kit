package io.fabric8.kit.enricher.api;

import java.util.Iterator;
import java.util.List;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author roland
 * @since 30.05.17
 */
public class EnricherFactoryTest {
    private EnricherFactory<ProjectContext> enricherFactory;

    @Before
    public void setup() {
        enricherFactory = new EnricherFactory<ProjectContext>();
    }

    @Test
    public void testOrder() {
        List<Enricher<ProjectContext>> enrichers =
                enricherFactory.createEnrichers("enricher/test-enrichers-default", "enricher/test-enrichers");
        String[] orderExpected = new String[] { "three", "two", "five", "one"};
        assertEquals(enrichers.size(), 4);
        Iterator<Enricher<ProjectContext>> it = enrichers.iterator();
        for (String val : orderExpected) {
            assertEquals(it.next().getName(),val);
        }
    }

    @Test
    public void errorHandling() {
        try {
            enricherFactory.createEnrichers("enricher/error-enrichers");
            fail();
        } catch (IllegalStateException exp) {
            assertTrue(exp.getMessage().matches(".*bla\\.blub\\.NotExist.*"));
        }
    }

    public static class Test1 extends TestEnricher { public Test1() { super("one"); }}
    public static class Test2 extends TestEnricher { public Test2() { super("two"); }}
    public static class Test3 extends TestEnricher { public Test3() { super("three"); }}
    public static class Test4 extends TestEnricher { public Test4() { super("four"); }}
    public static class Test5 extends TestEnricher { public Test5() { super("five"); }}

    static class TestEnricher implements Enricher<ProjectContext> {

        private final String name;

        TestEnricher(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<HasMetadata> convert(Platform platform, EnricherContext<ProjectContext> context, List<HasMetadata> items) {
            return null;
        }

        @Override
        public void create(Platform platform, EnricherContext<ProjectContext> context, KubernetesListBuilder builder) {

        }

        @Override
        public void enrich(Platform platform, EnricherContext<ProjectContext> context, KubernetesListBuilder builder) {

        }
    }

}
