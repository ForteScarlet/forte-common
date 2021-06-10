/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     _Useful.kt
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

@file:JvmName("#Useful")
package love.forte.common

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


/**
 * 验证所有的值都是指定类型。
 */
@OptIn(ExperimentalContracts::class)
public inline fun <reified T> bothAre(t1: Any, t2: Any): Boolean {
    contract {
        returns(true) implies (t1 is T && t2 is T)
    }

    return t1 is T && t2 is T
}

/**
 * 验证所有的值都是指定类型。
 */
public inline fun <reified T> allAre(vararg t: Any): Boolean = t.all { it is T }

/**
 * 验证所有的值都不是指定类型。
 */
public inline fun <reified T> bothNot(t1: Any, t2: Any): Boolean = t1 !is T && t2 !is T

/**
 * 验证所有的值都不是指定类型。
 */
public inline fun <reified T> allNot(vararg t: Any): Boolean = t.all { it !is T }


/**
 * 如果是某个类型，使用，否则计算默认值。
 */
public inline fun <reified T> ifOr(it: Any, orDef: (Any) -> T): T = if (it is T) it else orDef(it)

/**
 * 如果是某个类型，使用，否则用默认值。
 */
public inline fun <reified T> ifOr(it: Any, orDef: T): T = if (it is T) it else orDef



public inline fun <reified T, K> listAs(list: List<K>): List<T>? {
    @Suppress("UNCHECKED_CAST")
    return if (list.all { it is T }) list as List<T> else null
}
