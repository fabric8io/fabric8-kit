package io.fabric8.kit.build.service.docker.access.hc.util;
/*
 *
 * Copyright 2016 Roland Huss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;

import org.apache.http.impl.client.CloseableHttpClient;

/**
 * A client builder know how to build HTTP clients
 *
 * @author roland
 * @since 03/05/16
 */
public interface ClientBuilder {

    /**
     * Create a pooled client
     *
     * @return an HTTP client
     * @throws IOException
     */
    CloseableHttpClient buildPooledClient() throws IOException;

    /**
     * Create a basic client with a single connection. This is the client which should be used
     * in long running threads
     *
     * @return an HTTP client
     * @throws IOException
     */
    CloseableHttpClient buildBasicClient() throws IOException;

}
