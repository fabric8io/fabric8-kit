package io.fabric8.kit.generator.api;

import java.util.Iterator;
import java.util.List;

import io.fabric8.kit.config.image.ImageConfiguration;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author roland
 * @since 30.05.17
 */
public class GeneratorFactoryTest {
    private GeneratorFactory<ProjectContext> generatorFactory;

    @Before
    public void setup() {
        generatorFactory = new GeneratorFactory<ProjectContext>();
    }

    @Test
    public void testOrder() {
        List<Generator<ProjectContext>> generators =
                generatorFactory.createGenerators("generator/test-generators-default", "generator/test-generators");
        String[] orderExpected = new String[] { "three", "two", "five", "one"};
        assertEquals(generators.size(), 4);
        Iterator<Generator<ProjectContext>> it = generators.iterator();
        for (String val : orderExpected) {
            assertEquals(it.next().getName(),val);
        }
    }

    @Test
    public void errorHandling() {
        try {
            generatorFactory.createGenerators("generator/error-generators");
            fail();
        } catch (IllegalStateException exp) {
            assertTrue(exp.getMessage().matches(".*bla\\.blub\\.NotExist.*"));
        }
    }

    public static class Test1 extends TestGenerator { public Test1() { super("one"); }}
    public static class Test2 extends TestGenerator { public Test2() { super("two"); }}
    public static class Test3 extends TestGenerator { public Test3() { super("three"); }}
    public static class Test4 extends TestGenerator { public Test4() { super("four"); }}
    public static class Test5 extends TestGenerator { public Test5() { super("five"); }}

    static class TestGenerator implements Generator<ProjectContext> {

        private final String name;

        TestGenerator(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isApplicable(Platform platform, GeneratorContext<ProjectContext> context, List<ImageConfiguration> configs) {
            return false;
        }

        @Override
        public void generate(Platform platform, GeneratorContext<ProjectContext> context, List<ImageConfiguration> configs) {

        }
    }

}
