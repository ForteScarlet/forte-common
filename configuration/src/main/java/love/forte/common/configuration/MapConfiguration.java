/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     MapConfiguration.java
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

package love.forte.common.configuration;

import java.util.Map;

/**
 *
 * 定义以Map作为Config
 *
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
public interface MapConfiguration extends Configuration, Map<String, ConfigurationProperty> {
    /**
     * 判断是否存在配置内容。
     * @return 是否存在配置内容
     */
    @Override
    default boolean isEmpty() {
        return size() == 0;
    }
}
