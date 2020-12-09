/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     _SequencesEx.kt
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

@file:JvmName("SequencesExpandFunctions")
package love.forte.common.sequences


/**
 * 以自定义规则去重，并且在出现重复的时候对重复的内容进行重新定义。（或者用于抛出异常。）
 * [merger] 的两个参数中，[K] 代表 [selector] 所选择的用于去重的当前元素内容，[T] 则代表这个出现了重复的当前元素。
 *
 * 如果 [merger] 返回值为null，则会忽略掉当前重复值。
 *
 * @throws IllegalStateException 如果 [merger] 后的元素依旧存在重复情况。
 *
 */
public fun <T, K> Sequence<T>.distinctByMerger(selector: (T) -> K, merger: (K, T) -> T?): Sequence<T> {
    return DistinctMergerSequence(this, selector, merger)
}

/**
 * 去重，并且在出现重复的时候对重复的内容进行重新定义。（或者用于抛出异常。）
 * [merger] 中的参数 [T] 代表出现了重复的元素，返回值则代表对此重复元素的最终处理。
 *
 * 如果 [merger] 返回值为null，则会忽略掉当前重复值。
 *
 * @throws IllegalStateException 如果 [merger] 后的元素依旧存在重复情况。
 *
 */
public fun <T> Sequence<T>.distinctByMerger(merger: (T) -> T?): Sequence<T> {
    return DistinctMergerSequence<T, T>(this, merger)
}
