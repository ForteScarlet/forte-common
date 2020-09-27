/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     DefaultConfigurationParserRegistry.java
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

package love.forte.common.configuration;

import love.forte.common.configuration.impl.LinkedConfigurationParserManagerBuilder;
import love.forte.common.configuration.impl.PropertiesParser;
import love.forte.common.configuration.impl.YamlParser;
import love.forte.common.utils.convert.ConverterManager;
import love.forte.common.utils.convert.HutoolConverterManagerBuilderImpl;

/**
 * 基础配置解析器的注册器，提供注册默认解析器的方法与获取一个默认manager的方法。
 *
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 * @see #register(ConfigurationParserManagerBuilder)
 */
public class ConfigurationManagerRegistry {

    /**
     * 获取一个默认的解析器。 此解析器提供了基础的properties解析与yml解析。
     * @return {@link ConfigurationParserManager}
     */
    public static ConfigurationParserManager defaultManager() {
        final ConverterManager converterManager = new HutoolConverterManagerBuilderImpl().build();
        final LinkedConfigurationParserManagerBuilder builder = new LinkedConfigurationParserManagerBuilder(converterManager);
        return register(builder).build();
    }

    /**
     * 注册几个默认解析器到 {@link ConfigurationParserManagerBuilder} 中。
     *
     * @param builder {@link ConfigurationParserManagerBuilder}
     * @see PropertiesParser
     * @see YamlParser
     */
    public static <B extends ConfigurationParserManagerBuilder> B register(B builder) {
        // properties 解析器
        builder.register("properties", PropertiesParser.INSTANCE);
        builder.register("lang", PropertiesParser.INSTANCE);
        // yaml 解析器，会注册 yaml和yml两个类型
        builder.register("yaml", YamlParser.INSTANCE);
        builder.register("yml", YamlParser.INSTANCE);
        return builder;
    }


}
