/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     MergedIterator.kt
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

package love.forte.common.collections


/**
 * merged iterator. 合并两个iterator.
 */
internal class MergedIterator<T>(
    private val it1: Iterator<T>,
    private val it2: Iterator<T>
) : Iterator<T> {

    /**
     * if `true`, use it1.
     *
     * if `false`, use it2.
     *
     * if `null`, no more next.
     */
    private var whichOne: Boolean? = reFlashWhichOne()


    private fun reFlashWhichOne(): Boolean? {
        whichOne = when {
            it1.hasNext() -> true
            it2.hasNext() -> false
            else -> null
        }
        return whichOne
    }

    /**
     * Returns `true` if the iteration has more elements.
     */
    override fun hasNext(): Boolean = reFlashWhichOne() != null

    /**
     * Returns the next element in the iteration.
     */
    override fun next(): T {
        return when(whichOne) {
            true -> it1.next()
            false -> it2.next()
            else -> throw NoSuchElementException("no more next.")
        }
    }
}
