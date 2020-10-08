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
 * 去重，并且在出现重复的时候进行合并。（或者用于抛出异常。）
 */
public fun <T, K> Sequence<T>.distinctByMerger(selector: (T) -> K, merger: (K, T) -> T): Sequence<T> {
    return DistinctMergerSequence(this, selector, merger)
}

/**
 * 去重，并且在出现重复的时候进行合并。（或者用于抛出异常。）
 */
public fun <T> Sequence<T>.distinctByMerger(merger: (T, T) -> T): Sequence<T> {
    return DistinctMergerSequence(this, { it }, merger)
}
