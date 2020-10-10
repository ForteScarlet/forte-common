/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     EmptyIterator.kt
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

package love.forte.common.collections

/**
 * Empty Iterator.
 */
internal object EmptyIterator: Iterator<Nothing> {
    /**
     * Returns `true` if the iteration has more elements.
     */
    override fun hasNext(): Boolean = false

    /**
     * Returns the next element in the iteration.
     */
    override fun next(): Nothing = throw NoSuchElementException("No more next.")
}