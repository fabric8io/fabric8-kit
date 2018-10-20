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

package io.fabric8.kit.enricher.api;

import java.util.List;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesListBuilder;

/**
 * Interface describing <em>Enrichers</em> which are here to enrich a given list of resource objects:
 *
 * <ul>
 *     <li>By <em>creating</em> missing objects</li>
 *     <li>By <em>enriching</em> existing objects</li>
 * </ul>
 *
 * @author roland
 * @since 01/04/16
 */
public interface Enricher<P extends ProjectContext> {

    /**
     * The name of the enricher
     * @return unique enricher name
     */
    String getName();

    /**
     * Examine a given list of Kubernetes objects and convert them to target platform elements.
     * Only the resource types which can be converted by this converter should be picked up and returned.
     * All others should be ignored.
     *
     * @param platform the platform to convert to
     * @param context context holding build information
     * @param items list of resource objects to examine
     * @return the objects which could be converted. Can be empty but not null.
     */
    List<HasMetadata> convert(Platform platform, EnricherContext<P> context, List<HasMetadata> items);

    /**
     * Create resource objects and add them to the given resource list which is provided as a builder object.
     *
     * @param platform platform (OpenShift / Kubernetes) for which to create the objects
     * @param context context holding build information
     * @param builder builder to add to
     */
    void create(Platform platform, EnricherContext<P> context, KubernetesListBuilder builder);

    /**
     * Enrich a given list with additional information. No new elements must be added to the builder.
     *
     * @param platform target platform for which to add the objects
     * @param context context holding build information
     * @param builder the builder which holds the given objects
     */
    void enrich(Platform platform, EnricherContext<P> context, KubernetesListBuilder builder);
}
