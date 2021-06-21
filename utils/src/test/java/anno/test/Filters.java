/*
 *
 *  * Copyright (c) 2020. ForteScarlet All rights reserved.
 *  * Project  simple-robot
 *  * File     MiraiAvatar.kt
 *  *
 *  * You can contact the author through the following channels:
 *  * github https://github.com/ForteScarlet
 *  * gitee  https://gitee.com/ForteScarlet
 *  * email  ForteScarlet@163.com
 *  * QQ     1149159218
 *
 */

package anno.test;

import love.forte.common.utils.annotation.MixRepeatableAnnotations;

import java.lang.annotation.*;

/**
 * 监听函数过滤器。以注解的形式对监听函数进行匹配与过滤。
 *
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
@Retention(RetentionPolicy.RUNTIME)    //注解会在class字节码文件中存在，在运行时可以通过反射获取到
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE}) //接口、类、枚举、注解、方法
@Documented
@MixRepeatableAnnotations
public @interface Filters {

    Filter[] value() default {};

    String target() default "";

    String[] customFilter() default {};

    String[] codes() default {};

    String[] groups() default {};

    String[] bots() default {};

    boolean atBot() default false;

    boolean anyAt() default false;

    String[] at() default {};

}
