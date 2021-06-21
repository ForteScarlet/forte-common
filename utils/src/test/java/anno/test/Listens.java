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
 * 标注一个监听函数。
 *
 * 只能标注在方法上。
 *
 *
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Documented
@MixRepeatableAnnotations
public @interface Listens {

    Listen[] value();

    int priority() default 1;
    String name() default "";
}
