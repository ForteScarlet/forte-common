/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     CollectionsEx.kt
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

@file:JvmName("CollectionsExpand")

package love.forte.common.collections


/*
    为kotlin提供一些对于collection相关的额外的方法.
 */

/**
 * Simple Empty.
 */
data class SimpleEntry<K, V>(override val key: K, override val value: V) : Map.Entry<K, V>


/**
 * Array set, simple [Set] by [Array].
 */
data class ArraySet<T>(private val array: Array<T>) : Set<T> {

    override val size: Int
        get() = array.size

    override fun contains(element: T): Boolean = array.contains(element)

    override fun containsAll(elements: Collection<T>): Boolean = elements.all { contains(it) }

    override fun isEmpty(): Boolean = array.isEmpty()

    override fun iterator(): Iterator<T> = array.iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArraySet<*>

        if (!array.contentEquals(other.array)) return false

        return true
    }

    override fun hashCode(): Int {
        return array.contentHashCode()
    }

}

/**
 * array set of.
 */
public fun <T> arraySetOf(vararg value: T) : Set<T> = ArraySet(value)



