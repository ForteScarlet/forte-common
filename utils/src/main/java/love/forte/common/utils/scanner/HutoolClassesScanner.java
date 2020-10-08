/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     HutoolClassesScanner.java
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

package love.forte.common.utils.scanner;

import cn.hutool.core.lang.ClassScanner;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * 使用hutool实现的类扫描器。
 *
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
public class HutoolClassesScanner implements Scanner<String, Class<?>> {

    private List<ClassScanner> scanners = new LinkedList<>();

    /**
     * 寻找某个路径下符合匹配规则的目标。
     *
     * @param path   路径。
     * @param filter 匹配规则。
     * @return 扫描器本身，链式调用。
     */
    @Override
    public HutoolClassesScanner scan(String path, Predicate<Class<?>> filter) {
        scanners.add(new ClassScanner(path, filter::test));
        return this;
    }

    /**
     * 寻找某个路径下的目标。
     *
     * @param path 路径。
     * @return 扫描器本身，链式调用。
     */
    @Override
    public HutoolClassesScanner scan(String path) {
        scanners.add(new ClassScanner(path));
        return this;
    }

    /**
     * 获取最终的扫描结果，并作为一个集合返回。
     *
     * @return 最终的扫描结果
     */
    @Override
    public Set<Class<?>> getCollection() {
        final List<ClassScanner> scanners = this.scanners;
        this.scanners = new LinkedList<>();
        return scanners.stream().flatMap(scanner -> scanner.scan().stream()).collect(Collectors.toSet());
    }
}
