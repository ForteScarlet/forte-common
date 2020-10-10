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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

/**
 * 基础配置解析器的注册器，提供注册默认解析器的方法与获取一个默认manager的方法。
 *
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 * @see #register(ConfigurationParserManagerBuilder)
 */
public class ConfigurationManagerRegistry {

    /**
     * 默认的解析器。
     */
    private final static Map<String, ConfigurationParser> DEFAULT_PARSER_MAP;

    static {
        DEFAULT_PARSER_MAP = new ConcurrentHashMap<>();
        // properties 解析器，注册 properties类型和lang类型。
        DEFAULT_PARSER_MAP.put("properties", PropertiesParser.INSTANCE);
        DEFAULT_PARSER_MAP.put("lang", PropertiesParser.INSTANCE);
        // yaml 解析器，注册 yaml和yml两个类型
        DEFAULT_PARSER_MAP.put("yaml", YamlParser.INSTANCE);
        DEFAULT_PARSER_MAP.put("yml", YamlParser.INSTANCE);
    }

    /**
     * 注册一个新的（可能存在冲突的）默认解析器。
     * @param key 解析类型。
     * @param parser 解析器实例。
     * @param remappingFunction 合并函数（如果冲突）。
     */
    public static ConfigurationParser mergerDefaultParser(String key, ConfigurationParser parser, BiFunction<? super ConfigurationParser, ? super ConfigurationParser, ? extends ConfigurationParser> remappingFunction) {
        return DEFAULT_PARSER_MAP.merge(key, parser, remappingFunction);
    }

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
        DEFAULT_PARSER_MAP.forEach(builder::register);
        return builder;
    }


}
