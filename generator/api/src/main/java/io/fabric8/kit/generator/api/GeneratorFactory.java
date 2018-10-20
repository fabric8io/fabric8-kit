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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * A simple factory for creating services with no-arg constructors from a textual
 * descriptor. This descriptor, which must be a resource loadable by this class'
 * classloader, is a plain text file which looks like
 *
 * <pre>
 *   com.example.MyGenerator
 *   !io.fabric8.maven.fabric8.enhancer.DefaultProjectGenerator
 *   com.example.AnotherGenerator,50
 * </pre>
 *
 * If a line starts with <code>!</code> it is removed if it has been added previously.
 * The optional second numeric value is the order in which the services are returned.
 *
 * @author roland
 * @since 05.11.10
 */

final class GeneratorFactory<P extends ProjectContext> {

    private List<ClassLoader> classLoaders = new ArrayList<>();

    GeneratorFactory(ClassLoader... additionalClassLoaders) {
        Collections.addAll(classLoaders,
                           Thread.currentThread().getContextClassLoader(),
                GeneratorFactory.class.getClassLoader());
        Collections.addAll(classLoaders, additionalClassLoaders);
    }

    /**
     *
     * Create a list of generators ordered according to the ordering given in the
     * generator descriptor files. Note, that the de
     * scriptor will be looked up
     * in the whole classpath space, which can result in reading in multiple
     * descriptors with a single path. Note, that the reading order for multiple
     * resources with the same name is not defined.
     *
     * @param descriptorPaths a list of resource paths which are handle in the given order.
     *        Normally, default service should be given as first parameter so that custom
     *        descriptors have a chance to remove a default service.
     * @return a ordered list of created services or an empty list.
     */
     List<Generator<P>> createGenerators(String... descriptorPaths) {
        try {
            GeneratorEntry.initDefaultOrder();
            TreeMap<GeneratorEntry,Generator<P>> generatorMap = new TreeMap<>();
            for (String descriptor : descriptorPaths) {
                readGeneratorDefinitions(generatorMap, descriptor);
            }
            ArrayList<Generator<P>> ret = new ArrayList<>();
            ret.addAll(generatorMap.values());
            return ret;
        } finally {
            GeneratorEntry.removeDefaultOrder();
        }
    }

    // ===============================================================================================

    private void readGeneratorDefinitions(Map<GeneratorEntry, Generator<P>> extractorMap, String defPath) {
        try {
            Set<String> ret = new HashSet<>();
            for (ClassLoader cl : classLoaders) {
                Enumeration<URL> urlEnum = cl.getResources(defPath);
                ret.addAll(extractUrlAsStringsFromEnumeration(urlEnum));
            }
            for (String url : ret) {
                readGeneratorDefinitionFromUrl(extractorMap, url);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load service from " + defPath + ": " + e, e);
        }
    }

    private Set<String> extractUrlAsStringsFromEnumeration(Enumeration<URL> urlEnum) {
        Set<String> ret = new HashSet<>();
        while (urlEnum.hasMoreElements()) {
            ret.add(urlEnum.nextElement().toExternalForm());
        }
        return ret;
    }

    private void readGeneratorDefinitionFromUrl(Map<GeneratorEntry, Generator<P>> generatorMap, String url) {
        String line = null;
        try (LineNumberReader reader = new LineNumberReader(new InputStreamReader(new URL(url).openStream(), "UTF8"))) {
            line = reader.readLine();
            while (line != null) {
                createOrRemoveGenerator(generatorMap, line);
                line = reader.readLine();
            }
        } catch (ReflectiveOperationException|IOException e) {
            throw new IllegalStateException("Cannot load service " + line + " defined in " +
                                            url + " : " + e + ". Aborting", e);
        }
    }

    // Matches comment lines and empty lines. these are skipped
    private static Pattern COMMENT_LINE_PATTERN = Pattern.compile("^(\\s*#.*|\\s*)$");

    private synchronized void createOrRemoveGenerator(Map<GeneratorEntry, Generator<P>> generatorMap, String line)
        throws ReflectiveOperationException {
        if (line.length() > 0 && !COMMENT_LINE_PATTERN.matcher(line).matches()) {
            GeneratorEntry entry = new GeneratorEntry(line);
            if (entry.isRemove()) {
                // Removing is a bit complex since we need to find out
                // the proper key since the order is part of equals/hash
                // so we cant fetch/remove it directly
                Set<GeneratorEntry> toRemove = new HashSet<>();
                for (GeneratorEntry key : generatorMap.keySet()) {
                    if (key.getClassName().equals(entry.getClassName())) {
                        toRemove.add(key);
                    }
                }
                for (GeneratorEntry key : toRemove) {
                    generatorMap.remove(key);
                }
            } else {
                Class<Generator<P>> clazz = classForName(entry.getClassName(), Generator.class);
                Generator<P> service = clazz.newInstance();
                generatorMap.put(entry, service);
            }
        }
    }

    private <T> Class<T> classForName(String className, Class<?> baseClass) throws ClassNotFoundException {
        Set<ClassLoader> tried = new HashSet<>();
        for (ClassLoader loader : classLoaders) {
            while (loader != null) {
                try {
                    if (!tried.contains(loader)) {
                        Class tryClass = Class.forName(className, true, loader);
                        if (baseClass.isAssignableFrom(tryClass)) {
                            return tryClass;
                        }
                    }
                } catch (ClassNotFoundException ignored) {}
                tried.add(loader);
                loader = loader.getParent();
            }
        }
        throw new ClassNotFoundException("Class " + className + " could not be found or is not a subtype of " + baseClass);
    }


    // =============================================================================

     static class GeneratorEntry implements Comparable<GeneratorEntry> {
        private String className;
        private boolean remove;
        private Integer order;

        private static ThreadLocal<Integer> defaultOrderHolder = new ThreadLocal<Integer>() {

            /**
             * Initialise with start value for entries without an explicite order. 100 in this case.
             *
             * @return 100
             */
            @Override
            protected Integer initialValue() {
                return Integer.valueOf(100);
            }
        };

        /**
         * Parse an entry in the service definition. This should be the full qualified classname
         * of a service, optional prefixed with "<code>!</code>" in which case the service is removed
         * from the defaul list. An order value can be appened after the classname with a comma for give a
         * indication for the ordering of services. If not given, 100 is taken for the first entry, counting up.
         *
         * @param line line to parse
         */
        public GeneratorEntry(String line) {
            String[] parts = line.split(",");
            if (parts[0].startsWith("!")) {
                remove = true;
                className = parts[0].substring(1);
            } else {
                remove = false;
                className = parts[0];
            }
            if (parts.length > 1) {
                try {
                    order = Integer.parseInt(parts[1]);
                } catch (NumberFormatException exp) {
                    order = nextDefaultOrder();
                }
            } else {
                order = nextDefaultOrder();
            }
        }

        private Integer nextDefaultOrder() {
            Integer defaultOrder = defaultOrderHolder.get();
            defaultOrderHolder.set(defaultOrder + 1);
            return defaultOrder;
        }

        private static void initDefaultOrder() {
            defaultOrderHolder.set(100);
        }

        private static void removeDefaultOrder() {
            defaultOrderHolder.remove();
        }

        private String getClassName() {
            return className;
        }

        private boolean isRemove() {
            return remove;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            GeneratorEntry that = (GeneratorEntry) o;

            return className.equals(that.className);

        }

        @Override
        public int hashCode() {
            return className.hashCode();
        }

        /** {@inheritDoc} */
        public int compareTo(GeneratorEntry o) {
            int ret = this.order - o.order;
            return ret != 0 ? ret : this.className.compareTo(o.className);
        }
    }
}
