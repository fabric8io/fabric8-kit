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

package io.fabric8.build.config.resource;

/**
 * @author roland
 * @since 22/03/16
 */
public class ProbeConfig {

    // Initial delay in seconds before the probe is started.
    Integer initialDelaySeconds;

    // Timeout in seconds how long the probe might take
    Integer timeoutSeconds;

    // Command to execute for probing
    String exec;

    // Probe this URL
    String getUrl;

    // TCP port to probe
    String tcpPort;

    public Integer getInitialDelaySeconds() {
        return initialDelaySeconds;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public String getExec() {
        return exec;
    }

    public String getGetUrl() {
        return getUrl;
    }

    public String getTcpPort() {
        return tcpPort;
    }
}
