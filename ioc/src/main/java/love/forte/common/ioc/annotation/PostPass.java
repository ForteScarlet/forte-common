/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     PrePass.java
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

package love.forte.common.ioc.annotation;

import love.forte.common.utils.annotation.AnnotateMapping;

import java.lang.annotation.*;

/**
 *
 * init之后要执行的方法。
 *
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
@Retention(RetentionPolicy.RUNTIME)    //注解会在class字节码文件中存在，在运行时可以通过反射获取到
@Target({ElementType.METHOD}) //接口、类、枚举、注解、方法
@Documented
@Pass(postInit = true)
@AnnotateMapping(Pass.class)
public @interface PostPass {

    /**
     * 优先级。
     */
    int priority() default Integer.MAX_VALUE;
}
