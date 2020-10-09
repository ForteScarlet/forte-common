/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     MethodScanner.java
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

package love.forte.common.utils.scanner;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.function.Predicate;

/**
 *
 * 方法扫描器。
 *
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
public class MethodScanner implements Scanner<String, Method> {

    private Scanner<String, Class<?>> classScanner;

    public MethodScanner(){
        this.classScanner = new HutoolClassesScanner();
    }

    public MethodScanner(Scanner<String, Class<?>> classScanner){
        this.classScanner = classScanner;
    }

    /**
     * 寻找某个路径下符合匹配规则的目标。
     *
     * @param path   路径。
     * @param filter 匹配规则。
     * @return 扫描器本身，链式调用。
     */
    @Override
    public Scanner<String, Method> scan(String path, Predicate<Method> filter) {
        // todo
        return null;
    }

    /**
     * 获取最终的扫描结果，并作为一个集合返回。
     *
     * @return 最终的扫描结果
     */
    @Override
    public Collection<Method> getCollection() {
        // todo
        return null;
    }
}
