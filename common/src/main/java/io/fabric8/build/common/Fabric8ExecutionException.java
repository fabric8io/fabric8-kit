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
package io.fabric8.build.common;

/**
 * Checked exception signalling an unexpected result during execution of a task.
 *
 * @author nicola
 * @since 21/07/2017
 */
public class Fabric8ExecutionException extends Exception {

    public Fabric8ExecutionException() {
    }

    public Fabric8ExecutionException(String message) {
        super(message);
    }

    public Fabric8ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public Fabric8ExecutionException(Throwable cause) {
        super(cause);
    }

    public Fabric8ExecutionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
