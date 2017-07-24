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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

/**
 * Configuration for enrichers and generators
 *
 * @author roland
 * @since 24/07/16
 */
public class GeneratorsConfig<P extends ProjectContext> {

    public static final GeneratorsConfig EMPTY = new GeneratorsConfig();
    /**
     * Modules to includes, should hold <code>&lt;include&gt;</code> elements
     */
    //@JsonProperty(value = "includes")
    private List<String> includes = new ArrayList<>();

    /**
     * Modules to excludes, should hold <code>&lt;exclude&gt;</code> elements
     */
    //@JsonProperty(value = "excludes")
    private Set<String> excludes = new HashSet<>();

    /**
     * Configuration for enricher / generators
     */
    // See http://stackoverflow.com/questions/38628399/using-map-of-maps-as-maven-plugin-parameters/38642613 why
    // a "TreeMap" is used as parameter and not "Map<String, String>"
    //@JsonProperty(value = "config")
    private Map<String, TreeMap<String,String>> config = new HashMap<>();

    public GeneratorsConfig() { }

    public GeneratorsConfig(List<String> includes, Set<String> excludes, Map<String, TreeMap<String,String>> config) {
        this.includes = includes != null ? includes : Collections.<String>emptyList();
        this.excludes = excludes != null ? excludes : Collections.<String>emptySet();
        if (config != null) {
            this.config = config;
        }
    }

    public String getConfig(String name, String key) {
        TreeMap enricherMap =  config.get(name);
        return enricherMap != null ? (String) enricherMap.get(key) : null;
    }

    /**
     * Return full configuration as raw string-string values
     *
     * @param name name of the enricher / generator
     * @return unmodifiable map of the original config
     */
    public Map<String, String> getConfigMap(String name) {
        return config.containsKey(name) ?
            Collections.unmodifiableMap(config.get(name)) :
            Collections.<String, String>emptyMap();
    }


    /**
     * Override this for specific creating specific generator contexts
     *
     * @return context used during enrichment process.
     */
    public GeneratorContext<P> createGeneratorContext(P projectContext, String generatorName) {
        // TODO add prefix to the logger
        return new GeneratorContext<>(projectContext, getConfigMap(generatorName), projectContext.getLogger());
    }

    /**
     * Order elements according to the order provided by the include statements.
     * If no includes has been configured, return the given list unaltered.
     * Otherwise arrange the elements from the list in to the include order and return a new
     * list.
     *
     * If an include specifies an element which does not exist, an exception is thrown.
     *
     * @param generators the list to order
     * @return the ordered list according to the algorithm described above
     * @throws IllegalArgumentException if the includes reference an non existing element
     */
    public List<Generator<P>> filterGenerators(List<Generator<P>> generators) {
        List<Generator<P>> ret = new ArrayList<>();
        Map<String, Generator<P>> lookup = new HashMap<>();
        for (Generator<P> generator : generators) {
            lookup.put(generator.getName(), generator);
        }
        for (String inc : includes) {
            if (!excludes.contains(inc)) {
                Generator<P> named = lookup.get(inc);
                if (named == null) {
                    List<String> keys = new ArrayList<>(lookup.keySet());
                    Collections.sort(keys);
                    throw new IllegalArgumentException(
                        String.format("No generator with name '%s'" +
                        "' found to include. " +
                        "Please check spelling in your profile / config and your project dependencies. Included generators: %s", inc, StringUtils.join(keys,", ")));
                }
                ret.add(named);
            }
        }
        return ret;
    }

    /**
     * Merge in another processor configuration, with a lower priority. I.e. the latter a config is
     * in the argument list, the less priority it has. This means:
     *
     * <ul>
     *     <li>A configuration earlier in the list overrides configuration later</li>
     *     <li>Includes and exclude earlier in the list take precedence of the includes/excludes later in the list</li>
     * </ul>
     *
     * @param generatorsConfigs configs to merge into the current config
     * @return a merged configuration for convenience of chaining and returning. This is a new object.
     */
    @SafeVarargs
    public static <P extends ProjectContext> GeneratorsConfig<P> mergeGeneratorConfigs(GeneratorsConfig<P>... generatorsConfigs) {
        // Merge the configuration
        Map<String, TreeMap<String, String>> configs = mergeConfig(generatorsConfigs);

        // Get all includes
        Set<String> excludes = mergeExcludes(generatorsConfigs);

        // Find the set of includes, which are the ones from the profile + the ones configured
        List<String> includes = mergeIncludes(generatorsConfigs);

        return new GeneratorsConfig<>(includes, excludes, configs);
    }

    @SafeVarargs
    private static <P extends ProjectContext> Set<String> mergeExcludes(GeneratorsConfig<P>... configs) {
        Set<String> ret = new HashSet<>();
        for (GeneratorsConfig<P> config : configs) {
            if (config != null) {
                Set<String> excludes = config.excludes;
                if (excludes != null) {
                    ret.addAll(excludes);
                }
            }
        }
        return ret;
    }

    @SafeVarargs
    private static <P extends ProjectContext> List<String> mergeIncludes(GeneratorsConfig<P>... configs) {
        List<String> ret = new ArrayList<>();
        for (GeneratorsConfig<P> config : configs) {
            if (config != null) {
                List<String> includes = config.includes;
                if (includes != null) {
                    ret.addAll(includes);
                }
            }
        }
        return removeDups(ret);
    }

    // Remove duplicates such that the earlier element remains and the latter is removed
    // Only good for small list (that's what we expect for generators and generators)
    private static List<String> removeDups(List<String> list) {
        List<String> ret = new ArrayList<>();
        for (String el : list) {
            if (!ret.contains(el)) {
                ret.add(el);
            }
        }
        return ret;
    }

    @SafeVarargs
    private static <P extends ProjectContext> Map<String, TreeMap<String, String>> mergeConfig(GeneratorsConfig<P>... generatorsConfigs) {
        Map<String, TreeMap<String, String>> ret = new HashMap<>();
        if (generatorsConfigs.length > 0) {
            // Reverse iteration order so that earlier entries have a higher precedence
            for (int i = generatorsConfigs.length - 1; i >= 0; i--) {
                GeneratorsConfig<P> generatorsConfig = generatorsConfigs[i];
                if (generatorsConfig != null) {
                    if (generatorsConfig.config != null) {
                        for (Map.Entry<String, TreeMap<String,String>> entry : generatorsConfig.config.entrySet()) {
                            TreeMap<String, String> newValues = entry.getValue();
                            if (newValues != null) {
                                TreeMap<String, String> existing = ret.get(entry.getKey());
                                if (existing == null) {
                                    ret.put(entry.getKey(), new TreeMap<>(newValues));
                                } else {
                                    for (Map.Entry<String, String> newValue : newValues.entrySet()) {
                                        existing.put(newValue.getKey(), newValue.getValue());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ret.size() > 0 ? ret : null;
    }

}
