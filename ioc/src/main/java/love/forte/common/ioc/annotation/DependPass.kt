/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     DependPass.kt
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

package love.forte.common.ioc.annotation

import love.forte.common.ioc.DependBeanFactory


/**
 * 被标注了 [Pass] 注解的方法。
 */
public fun interface DependPass {
    /**
     * 执行此Pass。
     */
    operator fun invoke(dependBeanFactory: DependBeanFactory)
}
