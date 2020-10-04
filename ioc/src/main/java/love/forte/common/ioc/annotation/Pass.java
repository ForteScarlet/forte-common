/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     Pass.java
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

package love.forte.common.ioc.annotation;

import java.lang.annotation.*;

/**
 *
 * 标注一个方法，代表其在init前后所执行的方法。
 *
 * 只能标注在没有返回值的方法上。
 *
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
@Retention(RetentionPolicy.RUNTIME)    //注解会在class字节码文件中存在，在运行时可以通过反射获取到
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE}) //接口、类、枚举、注解、方法
@Documented
public @interface Pass {

    /**
     * 是否在init之前执行。
     */
    boolean preInit() default false;

    /**
     * 是否在init之后执行。
     */
    boolean postInit() default false;

}
