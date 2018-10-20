package io.fabric8.kit.config.image;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.fabric8.kit.common.KitLogger;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author roland
 * @since 17/05/16
 */
public class ConfigHelperTest {

    private boolean resolverCalled;
    private boolean customizerCalled;

    @Before
    public void setUp() throws Exception {
        resolverCalled = false;
        customizerCalled = false;
    }

    @Test
    public void noName() throws Exception {
        try {
            List<ImageConfiguration> configs = Arrays.asList(new ImageConfiguration.Builder().build());
            ConfigHelper.resolveImages(null, configs, createResolver(), null, createCustomizer());
            fail();
        } catch (IllegalArgumentException exp) {
            assertTrue(exp.getMessage().contains("name"));
        }

    }

    @Test
    public void simple() throws Exception {
        List<ImageConfiguration> configs = Arrays.asList(new ImageConfiguration.Builder().name("test").build());
        List<ImageConfiguration> result = ConfigHelper.resolveImages(null, configs, createResolver(), null, createCustomizer());
        assertEquals(1,result.size());
        assertTrue(resolverCalled);
        assertTrue(customizerCalled);
    }

    @Test
    public void filter() throws Exception {
        List<ImageConfiguration> configs = Arrays.asList(new ImageConfiguration.Builder().name("test").build());
        CatchingLog logCatcher = new CatchingLog();
        List<ImageConfiguration> result = ConfigHelper.resolveImages(
            logCatcher,
            configs, createResolver(), "bla", createCustomizer());
        assertEquals(0,result.size());
        assertTrue(resolverCalled);
        assertTrue(customizerCalled);
        assertTrue(logCatcher.getWarnMessage().contains("test"));
        assertTrue(logCatcher.getWarnMessage().contains("bla"));
    }

    @Test
    public void initAndValidate() throws Exception {
        List<ImageConfiguration> configs = Arrays.asList(new ImageConfiguration.Builder().name("test").build());
        String api = ConfigHelper.initAndValidate(configs, "v1.16", ConfigHelper.NameFormatter.IDENTITY, null);
        assertEquals("v1.16",api);
    }

    private ConfigHelper.Customizer createCustomizer() {
        return new ConfigHelper.Customizer() {
            @Override
            public List<ImageConfiguration> customizeConfig(List<ImageConfiguration> configs) {
                customizerCalled = true;
                return configs;
            }
        };
    }

    private ConfigHelper.Resolver createResolver() {
        return new ConfigHelper.Resolver() {
            @Override
            public List<ImageConfiguration> resolve(ImageConfiguration image) {
                resolverCalled = true;
                return Collections.singletonList(image);
            }
        };
    }

    private class CatchingLog implements KitLogger {
        private String warnMessage;

        void reset() {
            warnMessage = null;
        }

        public String getWarnMessage() {
            return warnMessage;
        }

        @Override
        public void debug(String format, Object... params) {

        }

        @Override
        public void info(String format, Object... params) {

        }

        @Override
        public void verbose(String format, Object... params) {

        }

        @Override
        public void warn(String format, Object... params) {
            if (params.length > 0) {
                warnMessage = String.format(format, params);
            } else {
                warnMessage = format;
            }
        }

        @Override
        public void error(String format, Object... params) {

        }

        @Override
        public boolean isDebugEnabled() {
            return false;
        }

        @Override
        public boolean isVerboseEnabled() {
            return false;
        }
    }

}
