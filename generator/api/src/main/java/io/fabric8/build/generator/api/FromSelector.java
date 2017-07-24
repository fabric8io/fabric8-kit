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

import java.util.Map;

/**
 * Helper interface to encapsulate the selection of a base image
 *
 * @author nicola
 * @since 17/07/17
 */
public interface FromSelector<P extends ProjectContext> {

    String getFrom(Platform platform, GeneratorContext<P> context, String imageType);

    boolean useImageStreamTag(Platform platform, GeneratorContext<P> context, String imageType);

    Map<String, String> getImageStreamTagFromExt(Platform platform, GeneratorContext<P> context, String imageType);

}
