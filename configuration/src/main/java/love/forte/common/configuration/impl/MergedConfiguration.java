/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     MergedConfiguration.java
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

package love.forte.common.configuration.impl;

import love.forte.common.configuration.Configuration;
import love.forte.common.configuration.ConfigurationProperty;
import love.forte.common.configuration.MapConfiguration;

import java.util.*;
import java.util.concurrent.atomic.LongAdder;

/**
 *
 * 合并的配置类，用于合并多个 {@link love.forte.common.configuration.Configuration} 实例。
 *
 * 自身继承 {@link LinkedMapConfiguration}，因此支持添加新的配置。
 *
 * 假如内部存在的所有 configuration中存在重复的值，则无法保证最终得到的值是谁的，
 * 且如果存在重复值，则 {@link #size()} 的值也会受到影响。
 *
 *
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
public class MergedConfiguration extends LinkedMapConfiguration implements Configuration {

    private final Queue<Configuration> configs;

    private MergedConfiguration(){
        this.configs = new ArrayDeque<>(0);
    }

    private MergedConfiguration(Collection<Configuration> configs){
        this.configs = new ArrayDeque<>(configs);
    }


    /**
     * 合并多个config。一般来讲都为前者优先度更大。
     */
    public static Configuration merged(Configuration... configurations) {
        return merged(Arrays.asList(configurations));
    }

    /**
     * 合并两个config。一般来讲都为前者优先度更大。
     */
    public static Configuration merged(Configuration c1, Configuration c2) {
        if (c1 instanceof MapConfiguration && c2 instanceof MapConfiguration) {
            LinkedMapConfiguration map = new LinkedMapConfiguration();
            map.putAll((MapConfiguration)c2);
            map.putAll((MapConfiguration)c1);
            return map;
        } else {
            return new MergedConfiguration(Arrays.asList(c1, c2));
        }
    }

    /**
     * 合并多个config。一般来讲都为前者优先度更大。
     */
    public static Configuration merged(List<Configuration> configurations) {
        if (configurations.size() == 0) {
            return new MergedConfiguration();
        } else if (configurations.size() == 1){
            Configuration single = configurations.get(0);
            if (single instanceof MapConfiguration) {
                return new LinkedMapConfiguration((MapConfiguration) single);
            } else {
                return new MergedConfiguration(Collections.singleton(single));
            }
        } else {
            List<Configuration> configurationCopy = new ArrayList<>();
            int mapSize = 0;
            for (Configuration configuration : configurations) {
                if (configuration instanceof MapConfiguration) {
                    mapSize++;
                }
                configurationCopy.add(configuration);
            }
            if (mapSize == configurationCopy.size()) {
                // all map.
                final LinkedMapConfiguration linkedMapConfiguration = new LinkedMapConfiguration();

                // 倒序遍历，使得前者覆盖后者。
                for (int i = configurationCopy.size() - 1; i >= 0; i--) {
                    LinkedMapConfiguration configuration = (LinkedMapConfiguration)configurationCopy.get(i);
                    linkedMapConfiguration.putAll(configuration);
                }

                return linkedMapConfiguration;
            } else {
                return new MergedConfiguration(configurationCopy);
            }
        }
    }


    /**
     * 获取一项配置信息。
     * <p>
     * 其中，key一般是 `aaa.bbb.cccCCC`的格式，
     * 或者 `aaa.bbb.ccc-ccc`的格式。
     *
     * @param key 这项配置的键。
     * @return 得到的信息
     */
    @Override
    public ConfigurationProperty getConfig(String key) {
        for (Configuration config : configs) {
            synchronized (config) {
                final ConfigurationProperty getConfig = config.getConfig(key);
                if (getConfig != null) {
                    return getConfig;
                }
            }
        }
        return super.getConfig(key);
    }

    /**
     * 添加一项配置信息。
     *
     * 如果此key已经存在于其他的某个config中，则会覆盖那个配置，
     *
     * 否则此项新配置会存在当前配置中而不会修改任何内部的其他配置。
     * @param key    键
     * @param config 配置信息
     * @return 如果有旧的配置被覆盖了，则此为旧配置信息。
     */
    @Override
    public ConfigurationProperty setConfig(String key, ConfigurationProperty config) {
        for (Configuration configuration : configs) {
            synchronized (configuration) {
                if (configuration.containsConfig(key)) {
                    return configuration.setConfig(key, config);
                }
            }
        }
        return super.setConfig(key, config);
    }

    /**
     * 判断是否存在某个键。
     *
     * @param key 配置键。
     * @return 是否存在。
     */
    @Override
    public boolean containsConfig(String key) {
        for (Configuration config : configs) {
            synchronized (config) {
                if(config.containsConfig(key)) {
                    return true;
                }
            }

        }
        return super.containsConfig(key);
    }

    /**
     * 获取配置的数量。
     *
     * @return 数量
     */
    @Override
    public int size() {
        LongAdder adder = new LongAdder();
        for (Configuration config : configs) {
            adder.add(config.size());
        }
        adder.add(super.size());

        return adder.intValue();
    }

}
