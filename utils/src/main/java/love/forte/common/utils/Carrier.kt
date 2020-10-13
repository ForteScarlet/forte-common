/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     Carrier.kt
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */
@file:JvmName("Carriers")
package love.forte.common.utils

import java.util.*

/**
 * 一个类似于 java 的 [java.util.Optional] 类,
 * 提供一些简单的api来对一个可能为null的值进行操作。
 *
 * 其中的大部分方法为 `inline` 方法，能够有效提高kotlin代码对其调用时的效率。（大概
 *
 * 其代表了一个 **回执**, 而这个回执不确定其是否为 `null`。
 *
 */
@Suppress("UNCHECKED_CAST")
public data class Carrier<T>(private val value: T?) {
    /** 转化为java8的 [java.util.Optional] */
    public fun toOptional(): Optional<T> = Optional.ofNullable(value)

    /**
     * 得到 [value] 值。但是如果 [value] 为null，抛出异常。
     * @throws NullPointerException 如果 [value] 为null
     */
    public fun get(): T = value ?: throw NullPointerException("value is null.")

    /**
     * 得到 [value] 值
     * @return T?
     */
    public fun orNull(): T? = value

    /**
     * 得到 [value] 值，或者抛出一个异常。默认为抛出 [NullPointerException]
     */
    @JvmOverloads
    public fun orThrow(err: Throwable = NullPointerException("value is null.")): T = value ?: throw err


    /**
     * 得到 [value] 值，或者通过函数获取并抛出一个异常。默认为抛出 [NullPointerException]
     * @param err in java: ` getOrThrow(() -> new RuntimeException()) `
     */
    public inline fun orThrow(err: () -> Throwable): T = orNull() ?: throw err()


    /**
     * 得到 [value] 值，如果为null则获取一个默认值 [or]
     * @param or T
     * @return T
     */
    public fun orElse(or: T): T = value ?: or


    /**
     * 得到 [value] 值，如果为null就通过 [or] 计算并获取一个默认值
     * @param or Function0<T>
     * @return T
     */
    public inline fun orElse(or: () -> T): T = orNull() ?: or()


    /**
     * 如果值不为null，则进行转化
     */
    public inline fun <R> map(mapper: (T) -> R): Carrier<R> = orNull()?.let { Carrier(mapper(it)) } ?: empty()


    /**
     * 如果值符合期望，保留，否则变为null
     * @param filter Function1<T, Boolean>
     * @return Carrier<T>
     */
    public inline fun ifOr(filter: (T) -> Boolean): Carrier<T> = orNull()?.let {
        if (filter(it)) this else empty()
    } ?: empty()


    companion object {
        private val EMPTY: Carrier<Nothing> = Carrier(null)

        @JvmStatic
        public fun <T> empty(): Carrier<T> = EMPTY as Carrier<T>

        @JvmStatic
        public fun <T> get(value: T?): Carrier<T> {
            return when {
                value is Boolean -> (if (value) TrueCarrier else FalseCarrier)
                value is String && value.isEmpty() -> EmptyStringCarrier
                else -> value?.let { Carrier(value) } ?: empty()
            }  as Carrier<T>

        }
    }
}

/** 值为true的布尔 [Carrier]. */
internal val TrueCarrier: Carrier<Boolean> = Carrier(true)

/** 值为false的布尔 [Carrier]. */
internal val FalseCarrier: Carrier<Boolean> = Carrier(false)

/** 空字符串的 [Carrier]. */
internal val EmptyStringCarrier: Carrier<String> = Carrier("")

/**
 * 将一个任意的值转化为 [Carrier]
 */
@Suppress("RedundantVisibilityModifier")
public fun <T> T?.toCarrier(): Carrier<T> = Carrier.get(this)


