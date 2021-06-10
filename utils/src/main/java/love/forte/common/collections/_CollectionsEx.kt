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

@file:JvmName("CollectionExpands")
@file:Suppress("unused")

package love.forte.common.collections

import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 简单的entry实例。
 */
public fun <K, V> entry(k: K, v: V): Map.Entry<K, V> = SimpleEntry(k, v)

/**
 * 简单的entry实例。
 */
public fun <K, V> mutableEntry(k: K, v: V): MutableMap.MutableEntry<K, V> = SimpleMutableEntry(k, v)


/**
 * Simple Empty.
 * TODO private.
 */
data class SimpleEntry<K, V>(override val key: K, override val value: V) : Map.Entry<K, V>



private data class SimpleMutableEntry<K, V>(override val key: K, private var _value: V) :
    MutableMap.MutableEntry<K, V> {
    override val value: V
        get() = _value

    override fun setValue(newValue: V): V = _value.also { _value = newValue }
}





/**
 * Array set, simple [Set] by [Array].
 */
private data class ArraySet<T>(private val array: Array<T>) : Set<T> {

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
 * 一个简单的紧凑Set，与其说紧凑Set，不如说更接近于一个“ListSet”。
 * 适用于元素量很少，但是大于1的情况，例如2~3个固定元素。
 */
private data class CompactSet<T>(private val value: Collection<T>) : Set<T> {
    override val size: Int get() = value.size
    override fun contains(element: T): Boolean = value.contains(element)
    override fun containsAll(elements: Collection<T>): Boolean = value.containsAll(elements)
    override fun isEmpty(): Boolean = value.isEmpty()
    override fun iterator(): Iterator<T> = value.iterator()
}


/**
 * array set of.
 */
public fun <T> arraySetOf(vararg value: T): Set<T> = ArraySet(value)

/**
 * array set of.
 */
public inline fun <reified T> arraySetOf(value: Collection<T>): Set<T> = arraySetOf(*value.toTypedArray())

/**
 * 紧凑set of.
 */
public fun <T> compactSetOf(value: Collection<T>): Set<T> = CompactSet(value.toList())

/**
 * 紧凑set of.
 */
public fun <T> Sequence<T>.toCompactSet(): Set<T> = CompactSet(this.toList())




//**************************************
//*             Queue of.
//**************************************

/**
 * Queue of.
 */
public fun <T> queueOf(vararg elements: T): Queue<T> =
    if (elements.isEmpty()) LinkedList() else LinkedList(elements.toList())


/**
 * Concurrent queue of.
 */
public fun <T> concurrentQueueOf(vararg elements: T): Queue<T> =
    if (elements.isEmpty()) ConcurrentLinkedQueue() else ConcurrentLinkedQueue(elements.toList())


//**************************************
//*             sorted queue.
//**************************************

/**
 * Sorted queue of.
 */
public fun <T> sortedQueueOf(comparator: Comparator<T>, vararg elements: T): Queue<T> =
    SortedQueue(comparator, *elements)

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
public fun <T> concurrentSortedQueueOf(comparator: Comparator<T>, vararg elements: T): Queue<T> =
    ConcurrentSortedQueue(comparator, *elements)

/**
 * Concurrent sorted queue of.
 */
public fun <T> concurrentSortedQueueOf(comparator: Comparator<T>): Queue<T> = ConcurrentSortedQueue(comparator)

/**
 * Concurrent sorted queue of.
 */
public fun <T> concurrentSortedQueueOf(vararg elements: T): Queue<T> = ConcurrentSortedQueue(*elements)


/**
 * an empty iterator.
 */
public fun <T> emptyIterator(): Iterator<T> = EmptyIterator

// /**
//  * an empty iterator.
//  */
// public fun <T> emptyIterator(@Suppress("UNUSED_PARAMETER") type: Class<T>): Iterator<T> = EmptyIterator


/**
 * Iter plus.
 */
public operator fun <T> Iterator<T>.plus(other: Iterator<T>): Iterator<T> = MergedIterator(this, other)


/**
 * 一个紧凑型的不可变Map。
 * 内部使用 [ArraySet] 和 [SimpleEntry] 使用，适用于键值对数量很少的情况。
 * 键值对越多，其效率越慢。本质是数组键值对遍历查询。
 */
private class CompactMap<K, V>
private constructor(override val entries: Set<Map.Entry<K, V>>) : Map<K, V> {
    constructor(vararg pair: Pair<K, V>): this(arraySetOf(pair.map { (k, v) -> entry(k, v) }))

    private lateinit var _keys: Set<K>

    /**
     * Returns a read-only [Set] of all keys in this map.
     */
    override val keys: Set<K>
        get() {
            if (!::_keys.isInitialized) {
                _keys = compactSetOf(entries.map { e -> e.key })
            }
            return _keys
        }

    /**
     * Returns the number of key/value pairs in the map.
     */
    override val size: Int get() = entries.size

    /**
     * Returns a read-only [Collection] of all values in this map. Note that this collection may contain duplicate values.
     */
    override val values: Collection<V>
        get() = TODO("Not yet implemented")

    /**
     * Returns `true` if the map contains the specified [key].
     */
    override fun containsKey(key: K): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Returns `true` if the map maps one or more keys to the specified [value].
     */
    override fun containsValue(value: V): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Returns the value corresponding to the given [key], or `null` if such a key is not present in the map.
     */
    override fun get(key: K): V? {
        TODO("Not yet implemented")
    }

    /**
     * Returns `true` if the map is empty (contains no elements), `false` otherwise.
     */
    override fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }
}





