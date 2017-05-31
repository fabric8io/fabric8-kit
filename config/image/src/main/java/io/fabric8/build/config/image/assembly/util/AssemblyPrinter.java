package io.fabric8.build.config.image.assembly.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.mapper.ClassAliasingMapper;
import io.fabric8.build.config.image.assembly.*;

/**
 * @author roland
 * @since 31.05.17
 */
public class AssemblyPrinter {

    public static String toXml(Assembly assembly) {
        if (assembly == null) {
            return null;
        }
        XStream xstream = new XStream();
        xstream.alias("assembly", Assembly.class);
        xstream.omitField(Assembly.class,"modelEncoding");

        xstream.alias("file", FileItem.class);
        xstream.alias("fileSet", FileSet.class);
        xstream.alias("containerDescriptorHandler", ContainerDescriptorHandlerConfig.class);
        xstream.alias("moduleSet", ModuleSet.class);
        xstream.alias("sources", ModuleSources.class);
        xstream.alias("binaries", ModuleBinaries.class);
        xstream.alias("dependencySet", DependencySet.class);
        xstream.alias("unpackOptions", UnpackOptions.class);
        xstream.alias("repository", Repository.class);
        xstream.alias("groupVersionAlignment", GroupVersionAlignment.class);

        registerIncludeExcludeAliases(
            xstream,
            DependencySet.class,
            ModuleSources.class,
            ModuleSet.class,
            FileSet.class,
            ModuleBinaries.class,
            UnpackOptions.class,
            Repository.class);

        registerStringAlias(xstream, Assembly.class, "componentDescriptor");
        registerStringAlias(xstream, GroupVersionAlignment.class, "exclude");
        registerStringAlias(xstream, Repository.class, "groupVersionAlignment");

        return xstream.toXML(assembly);
    }

    private static void registerIncludeExcludeAliases(XStream xstream, Class ... clazzz) {
        for (Class clazz : clazzz) {
            registerStringAlias(xstream, clazz, "include");
            registerStringAlias(xstream, clazz, "exclude");
        }
    }

    private static void registerStringAlias(XStream xstream, Class clazz, String tag) {
        ClassAliasingMapper mapper = new ClassAliasingMapper(xstream.getMapper());
        mapper.addClassAlias(tag, String.class);
        xstream.registerLocalConverter(clazz, tag + "s", new CollectionConverter(mapper));
    }
}
