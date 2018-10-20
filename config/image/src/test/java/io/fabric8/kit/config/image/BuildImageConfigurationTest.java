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

import java.io.File;

import io.fabric8.kit.common.KitLogger;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static io.fabric8.kit.config.image.ArchiveCompression.*;
import static org.junit.Assert.*;

/**
 * @author roland
 * @since 04/04/16
 */
@RunWith(JMockit.class)
public class BuildImageConfigurationTest {

    @Mocked
    KitLogger logger;

    @Test
    public void empty() {
        BuildImageConfiguration config = new BuildImageConfiguration();
        config.initAndValidate(logger);
        assertFalse(config.isDockerFileMode());
    }

    @Test
    public void simpleDockerfile() {
        BuildImageConfiguration config =
            new BuildImageConfiguration.Builder().
                dockerFile("src/docker/Dockerfile").build();
        config.initAndValidate(logger);
        assertTrue(config.isDockerFileMode());
        assertEquals(config.getDockerFile(),new File("src/docker/Dockerfile"));
    }

    @Test
    public void simpleDockerfileDir() {
        BuildImageConfiguration config =
            new BuildImageConfiguration.Builder().
                dockerFileDir("src/docker/").build();
        config.initAndValidate(logger);
        assertTrue(config.isDockerFileMode());
        assertEquals(config.getDockerFile(),new File("src/docker/Dockerfile"));
    }

    @Test
    public void DockerfileDirAndDockerfileAlsoSet() {
        BuildImageConfiguration config =
            new BuildImageConfiguration.Builder().
                dockerFileDir("/tmp/").
                dockerFile("Dockerfile").build();
        config.initAndValidate(logger);
        assertTrue(config.isDockerFileMode());
        assertEquals(config.getDockerFile(),new File("/tmp/Dockerfile"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void DockerfileDirAndDockerfileAlsoSetButDockerfileIsAbsoluteExceptionThrown() {
        BuildImageConfiguration config =
            new BuildImageConfiguration.Builder().
                dockerFileDir("/tmp/").
                dockerFile("/Dockerfile").build();
        config.initAndValidate(logger);
    }

    @Test
    public void deprecatedDockerfileDir() {
        AssemblyConfiguration assemblyConfig = new AssemblyConfiguration.Builder().dockerFileDir("src/docker").build();
        BuildImageConfiguration config =
            new BuildImageConfiguration.Builder().
                assembly(assemblyConfig).build();

        new Expectations() {{
            logger.warn(withSubstring("deprecated"));
        }};

        config.initAndValidate(logger);
        assertTrue(config.isDockerFileMode());
        assertEquals(config.getDockerFile(),new File("src/docker/Dockerfile"));
    }

    @Test
    public void dockerFileAndArchive() {
        BuildImageConfiguration config =
            new BuildImageConfiguration.Builder().
                dockerArchive("this").
                dockerFile("that").build();

        try {
            config.initAndValidate(logger);
        } catch (IllegalArgumentException expected) {
            return;
        }
        fail("Should have failed.");
    }

    @Test
    public void dockerArchive() {
        BuildImageConfiguration config =
            new BuildImageConfiguration.Builder().
                dockerArchive("this").build();
        config.initAndValidate(logger);

        assertFalse(config.isDockerFileMode());
        assertEquals(new File("this"), config.getDockerArchive());
    }

    @Test
    public void compression() {
        BuildImageConfiguration config =
            new BuildImageConfiguration.Builder().
                compression("gzip").build();
        assertEquals(gzip, config.getCompression());

        config = new BuildImageConfiguration.Builder().build();
        assertEquals(none, config.getCompression());

        config =
            new BuildImageConfiguration.Builder().
                compression(null).build();
        assertEquals(none, config.getCompression());
    }


}
