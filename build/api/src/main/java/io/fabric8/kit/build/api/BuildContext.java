package io.fabric8.kit.build.api;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.function.Function;

import io.fabric8.kit.common.KitLogger;
import io.fabric8.kit.config.image.build.BuildConfiguration;


/**
 * @author roland
 * @since 16.10.18
 */
public interface BuildContext {

    String getSourceDirectory();

    File getBasedir();

    String getOutputDirectory();

    Properties getProperties();

    Function<String, String> createInterpolator(String filter);

    File createImageContentArchive(String imageName, BuildConfiguration buildConfig, KitLogger log) throws IOException;

    RegistryContext getRegistryContext();

    default File inSourceDir(String path) {
        return inDir(getSourceDirectory(), path);
    }

    default File inOutputDir(String path) {
        return inDir(getOutputDirectory(), path);
    }

    default File inDir(String dir, String path) {
        File file = new File(path);
        if (file.isAbsolute()) {
            return file;
        }
        File absoluteSourceDir = new File(getBasedir(), dir);
        return new File(absoluteSourceDir, path);
    }
}
