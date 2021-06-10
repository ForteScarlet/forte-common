/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     SortedList.java
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */
package love.forte.common.collections

import java.util.*
import java.util.concurrent.ConcurrentSkipListMap


/**
 *
 * 自排序队列。
 * 使用 [有序表][SortedMap] 实现的自排序有序队列实现，
 * 通过指定一个排序规则实现存入元素的时候可自动排序，
 * 且可存在排序值相同的元素。
 *
 * 需要注意的是，不允许使用 [MutableIterator.remove] 方法.
 *
 * @property sortedMap 使用的有序表。
 *
 * @author [ ForteScarlet ](https://github.com/ForteScarlet)
 */
public open class SortedQueue<T>
internal constructor(
    private val sortedMap: SortedMap<T, Queue<T>>
) :
    AbstractQueue<T>(), Queue<T> {
    constructor() : this(sortedMap = TreeMap())
    constructor(comparator: Comparator<T>, vararg elements: T) : this(sortedMap = TreeMap(comparator)) {
        elements.forEach { add(it) }
    }

    constructor(vararg elements: T) : this() {
        elements.forEach { add(it) }
    }


    /**
     * 排序数量，即总共有多少种顺序
     */
    val sortedSize: Int
        get() = sortedMap.size

    /**
     * 总数量
     */
    override val size: Int
        get() = sortedMap.values.sumOf { it.size }


    override fun isEmpty(): Boolean = sortedMap.isEmpty()

    /**
     * offer element.
     */
    override fun offer(e: T): Boolean {
        return sortedMap.merge(e, queueOf(e)) { oldValue, value ->
            oldValue.apply { addAll(value) }
        } != null
    }


    /**
     * Retrieves and removes the head of this queue,
     * or returns {@code null} if this queue is empty.
     *
     * @return the head of this queue, or {@code null} if this queue is empty
     */
    override fun poll(): T? {
        return if (sortedMap.isEmpty()) {
            null
        } else {
            val fKey = sortedMap.firstKey()
            val fValue = sortedMap[fKey]!!
            val pollFirst: T = fValue.poll()
            if (fValue.isEmpty()) {
                sortedMap.remove(fKey)
            }
            pollFirst
        }
    }


    /**
     * Retrieves, but does not remove, the head of this queue,
     * or returns {@code null} if this queue is empty.
     *
     * @return the head of this queue, or {@code null} if this queue is empty
     */
    override fun peek(): T? {
        return sortedMap[sortedMap.firstKey()]?.peek()
    }

    /**
     * iterator.
     */
    override fun iterator(): MutableIterator<T> = iterator0()


    /**
     * Real function for iterator.
     */
    private fun iterator0(): MutableIterator<T> {
        return SortedListIterator()
    }


    // /** 缓存迭代列表。 */
    // private var bufferList: List<T>? = null

    /**
     * @see iterator
     */
    inner class SortedListIterator : MutableIterator<T> {

        private val entriesIterator = sortedMap.iterator()

        // next.
        private var nowEntry: MutableMap.MutableEntry<T, Queue<T>>?

        private var nowValueIter: MutableIterator<T>?


        init {
            val hasNext: Boolean = entriesIterator.hasNext()
            nowEntry = if (hasNext) entriesIterator.next() else null
            nowValueIter = nowEntry?.value?.iterator()
        }

        private fun nextEntry(): MutableMap.MutableEntry<T, Queue<T>> {
            return entriesIterator.next().apply { nowEntry = this }
        }

        /**
         * 下一个iter.
         */
        private fun nextIter(): MutableIterator<T> {
            do {
                val nextEntry = nextEntry()
                val nextIter = nextEntry.value.iterator().apply {
                    nowValueIter = this
                }
                val hasNext: Boolean = nextIter.hasNext()
            } while (!hasNext)
            return nowValueIter!!
        }


        private fun nextElement(): T {
            val nowIter = nowValueIter
            return when {
                nowIter == null -> nextIter().next()
                nowIter.hasNext() -> nowIter.next()
                else -> nextIter().next()
            }

        }

        override fun hasNext(): Boolean = (nowValueIter?.hasNext() ?: false) || entriesIterator.hasNext()


        override fun next(): T = nextElement()

        override fun remove() {
            nowValueIter?.remove()
            if (nowEntry?.value?.isEmpty() == true) {
                entriesIterator.remove()
                nextEntry()
            }
        }
    }
}


/**
 * 使用 [同步跳表][ConcurrentSkipListMap] 与 [同步队列][concurrentQueueOf] 实现线程安全的自排序队列。
 */
public class ConcurrentSortedQueue<T>
private constructor(private val sortedMap: SortedMap<T, Queue<T>>) : SortedQueue<T>(sortedMap) {

    constructor() : this(ConcurrentSkipListMap())
    constructor(comparator: Comparator<T>, vararg elements: T) : this(ConcurrentSkipListMap(comparator)) {
        elements.forEach { add(it) }
    }

    constructor(vararg elements: T) : this(ConcurrentSkipListMap()) {
        elements.forEach { add(it) }
    }


    /**
     * offer element by concurrent queue.
     */
    override fun offer(e: T): Boolean {
        return sortedMap.merge(e, concurrentQueueOf(e)) { oldValue, value ->
            oldValue.apply { addAll(value) }
        } != null
    }
}


