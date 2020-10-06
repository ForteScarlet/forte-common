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

package love.forte.common.ioc

import love.forte.common.ioc.annotation.Depend
import love.forte.common.utils.annotation.AnnotationUtil
import java.lang.reflect.Method


/**
 * 被标注了 [love.forte.common.ioc.annotation.Pass] 注解的方法。
 */
public interface DependPass {
    /**
     * 执行此Pass。
     */
    operator fun invoke(dependBeanFactory: DependBeanFactory)

    /**
     * 排序
     */
    @JvmDefault
    val priority: Int
        get() = Int.MAX_VALUE

}


/**
 * [DependPass] 通过 method 实例的实现。
 */
public class MethodDependPass(
    private val method: Method,
    override val priority: Int
) : DependPass {

    /**
     * 方法所在类型。
     */
    private val instanceType: Class<*> = method.declaringClass

    /**
     * 方法所需参数列表。
     */
    private val parameterTypes: List<Pair<Boolean, Class<*>>> = method.parameters.map {
        val orNull = AnnotationUtil.getAnnotation(it, Depend::class.java)?.orIgnore ?: false
        orNull to it.type
    }

    /**
     * 执行此method。
     */
    override fun invoke(dependBeanFactory: DependBeanFactory) {
        val instance: Any = dependBeanFactory[instanceType]

        // pms
        val params: Array<Any?> = arrayOfNulls(parameterTypes.size)

        parameterTypes.forEachIndexed { i, t ->
            val orNull: Boolean = t.first
            val type: Class<*> = t.second
            params[i] = if(orNull) dependBeanFactory.getOrNull(type) else dependBeanFactory[type]
        }

        // invoke.
        method(instance, *params)
    }

}







