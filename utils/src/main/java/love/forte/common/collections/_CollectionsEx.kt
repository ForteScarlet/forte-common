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

import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue


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



//**************************************
//*             Queue of.
//**************************************

/**
 * Queue of.
 */
public fun <T> queueOf(vararg elements: T): Queue<T> = if (elements.isEmpty()) LinkedList() else LinkedList(elements.toList())

/**
 * Concurrent queue of.
 */
public fun <T> concurrentQueueOf(vararg elements: T): Queue<T> = if (elements.isEmpty()) ConcurrentLinkedQueue() else ConcurrentLinkedQueue(elements.toList())



//**************************************
//*             sorted queue.
//**************************************

/**
 * Sorted queue of.
 */
public fun <T> sortedQueueOf(comparator: Comparator<T>, vararg elements: T): Queue<T> = SortedQueue(comparator, *elements)

/**
 * Sorted queue of.
 */
public fun <T> sortedQueueOf(comparator: Comparator<T>): Queue<T> = SortedQueue(comparator)

/**
 * Sorted queue of.
 */
public fun <T> sortedQueueOf(vararg elements: T): Queue<T> = SortedQueue(*elements)

/**
 * Concurrent sorted queue of.
 */
public fun <T> concurrentSortedQueueOf(comparator: Comparator<T>, vararg elements: T): Queue<T> = ConcurrentSortedQueue(comparator, *elements)

/**
 * Concurrent sorted queue of.
 */
public fun <T> concurrentSortedQueueOf(comparator: Comparator<T>): Queue<T> = ConcurrentSortedQueue(comparator)

/**
 * Concurrent sorted queue of.
 */
public fun <T> concurrentSortedQueueOf(vararg elements: T): Queue<T> = ConcurrentSortedQueue(*elements)










