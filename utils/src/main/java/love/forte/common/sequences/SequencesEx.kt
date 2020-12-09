/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     SequencesEx.kt
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

@file:JvmName("SequencesExpands")

package love.forte.common.sequences


internal class DistinctMergerSequence<T, K>(
    private val source: Sequence<T>,
    private val keySelector: (T) -> K,
    private val merger: (K, T) -> T?
) : Sequence<T> {

    constructor(
        source: Sequence<T>,
        merger: (T) -> T?

    ): this(source, { @Suppress("UNCHECKED_CAST") (it as K) }, { _, next -> merger(next) })

    override fun iterator(): Iterator<T> = DistinctMergerIterator(source.iterator(), keySelector, merger)
}


private class DistinctMergerIterator<T, K>(
    private val source: Iterator<T>,
    private val keySelector: (T) -> K,
    private val merger: (K, T) -> T?
) : AbstractIterator<T>() {

    private val observed = HashSet<K>()

    override fun computeNext() {

        while (source.hasNext()) {
            val next = source.next()
            val key = keySelector(next)

            val setNext: T? =
                if (!observed.add(key)) {
                    val m = merger(key, next)
                    if (m != null && !observed.add(keySelector(m))) {
                        throw IllegalStateException("Duplicate element merged: $m")
                    }
                    m
                } else next

            if (setNext != null) {
                setNext(setNext)
                return
            }
        }
        done()
        // if (source.hasNext()) {
        //     val next = source.next()
        //     val key = keySelector(next)
        //
        //     val setNext: T? =
        //         if (!observed.add(key)) {
        //             val m = merger(key, next)
        //             if (m != null && !observed.add(keySelector(m))) {
        //                 throw IllegalStateException("Duplicate element merged: $m")
        //             }
        //             m
        //         } else next
        //
        //     setNext(setNext)
        // } else {
        //
        // }
    }
}
