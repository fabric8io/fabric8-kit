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

package io.fabric8.build.enricher.api;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

/**
 * Configuration for enrichers and generators
 *
 * @author roland
 * @since 24/07/16
 */
public class EnrichersConfig<P extends ProjectContext> {

    public static final EnrichersConfig EMPTY = new EnrichersConfig();
    /**
     * Modules to includes, should hold <code>&lt;include&gt;</code> elements
     */
    @JsonProperty(value = "includes")
    private List<String> includes = new ArrayList<>();

    /**
     * Modules to excludes, should hold <code>&lt;exclude&gt;</code> elements
     */
    @JsonProperty(value = "excludes")
    private Set<String> excludes = new HashSet<>();

    /**
     * Configuration for enricher / generators
     */
    // See http://stackoverflow.com/questions/38628399/using-map-of-maps-as-maven-plugin-parameters/38642613 why
    // a "TreeMap" is used as parameter and not "Map<String, String>"
    @JsonProperty(value = "config")
    private Map<String, TreeMap<String,String>> config = new HashMap<>();

    public EnrichersConfig() { }

    public EnrichersConfig(List<String> includes, Set<String> excludes, Map<String, TreeMap<String,String>> config) {
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
     * Override this for specific creating specific enricher contexts
     *
     * @return context used during enrichment process.
     */
    public EnricherContext<P> createEnricherContext(P projectContext, String enricherName) {
        return new EnricherContext<P>(projectContext, getConfigMap(enricherName));
    }

    /**
     * Order elements according to the order provided by the include statements.
     * If no includes has been configured, return the given list unaltered.
     * Otherwise arrange the elements from the list in to the include order and return a new
     * list.
     *
     * If an include specifies an element which does not exist, an exception is thrown.
     *
     * @param enrichers the list to order
     * @return the ordered list according to the algorithm described above
     * @throws IllegalArgumentException if the includes reference an non existing element
     */
    public List<Enricher<P>> filterEnrichers(List<Enricher<P>> enrichers) {
        List<Enricher<P>> ret = new ArrayList<>();
        Map<String, Enricher<P>> lookup = new HashMap<>();
        for (Enricher<P> enricher : enrichers) {
            lookup.put(enricher.getName(), enricher);
        }
        for (String inc : includes) {
            if (!excludes.contains(inc)) {
                Enricher<P> named = lookup.get(inc);
                if (named == null) {
                    List<String> keys = new ArrayList<>(lookup.keySet());
                    Collections.sort(keys);
                    throw new IllegalArgumentException(
                        String.format("No enricher with name '%s'" +
                        "' found to include. " +
                        "Please check spelling in your profile / config and your project dependencies. Included enrichers: %s", inc, StringUtils.join(keys,", ")));
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
     * @param enrichersConfigs configs to merge into the current config
     * @return a merged configuration for convenience of chaining and returning. This is a new object.
     */
    @SafeVarargs
    public static <P extends ProjectContext> EnrichersConfig<P> mergeEnricherConfigs(EnrichersConfig<P>... enrichersConfigs) {
        // Merge the configuration
        Map<String, TreeMap<String, String>> configs = mergeConfig(enrichersConfigs);

        // Get all includes
        Set<String> excludes = mergeExcludes(enrichersConfigs);

        // Find the set of includes, which are the ones from the profile + the ones configured
        List<String> includes = mergeIncludes(enrichersConfigs);

        return new EnrichersConfig<>(includes, excludes, configs);
    }

    @SafeVarargs
    private static <P extends ProjectContext> Set<String> mergeExcludes(EnrichersConfig<P>... configs) {
        Set<String> ret = new HashSet<>();
        for (EnrichersConfig<P> config : configs) {
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
    private static <P extends ProjectContext> List<String> mergeIncludes(EnrichersConfig<P>... configs) {
        List<String> ret = new ArrayList<>();
        for (EnrichersConfig<P> config : configs) {
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
    // Only good for small list (that's what we expect for enrichers and generators)
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
    private static <P extends ProjectContext> Map<String, TreeMap<String, String>> mergeConfig(EnrichersConfig<P>... enrichersConfigs) {
        Map<String, TreeMap<String, String>> ret = new HashMap<>();
        if (enrichersConfigs.length > 0) {
            // Reverse iteration order so that earlier entries have a higher precedence
            for (int i = enrichersConfigs.length - 1; i >= 0; i--) {
                EnrichersConfig<P> enrichersConfig = enrichersConfigs[i];
                if (enrichersConfig != null) {
                    if (enrichersConfig.config != null) {
                        for (Map.Entry<String, TreeMap<String,String>> entry : enrichersConfig.config.entrySet()) {
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
