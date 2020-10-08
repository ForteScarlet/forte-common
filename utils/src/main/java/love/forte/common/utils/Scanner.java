/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     Scanner.java
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

package love.forte.common.utils;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * 扫描器，用于定义一种 “扫描” 行为。
 *
 * @param <P> 路径类型，代表“扫描路径”所使用的类型。例如包扫描，一般可以是 String 类型 或者 Package 类型。
 *
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
public interface Scanner<P, T> {


    /**
     * 寻找某个路径下符合匹配规则的目标。
     * @param path 路径。
     * @param filter 匹配规则。
     * @return 扫描器本身，链式调用。
     */
    Scanner<P, T> find(String path, Predicate<T> filter);

    /**
     * 寻找某个路径下的目标。
     * @param path 路径。
     * @return 扫描器本身，链式调用。
     */
    default Scanner<P, T> find(String path) {
        return find(path, t -> true);
    }


    /**
     * 获取最终的扫描结果，并作为一个集合返回。
     * @return 最终的扫描结果
     */
    Collection<T> getCollection();


    /**
     * 获取最终的扫描结果，并作为一个流返回。
     * @return 最终的扫描结果
     */
    Stream<T> getStream();

}
