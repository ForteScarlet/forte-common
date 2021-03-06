/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     SimbotTime.kt
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

@file:Suppress("MemberVisibilityCanBePrivate", "unused", "RedundantVisibilityModifier")
@file:JvmName("TimeInline")

package love.forte.common.utils

import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


/**
 * 时间**内联类**。[time]参数是 **毫秒** 值。
 *
 * **仅用在kotlin中。**
 *
 * 你可以通过 中缀函数 [Number].[timeBy] ([TimeUnit]) 得到此实例，例如：
 * ```
 *  val second: Long = 20L
 *  val time: Time = time timeBy TimeUnit.SECONDS
 * ```
 * 你也可以直接通过函数 [now] 来得到当前时间的 [Time] 值。
 */
@JvmInline
public value class Time(val time: Long) {
    private val unit: TimeUnit get() = TimeUnit.MILLISECONDS
    fun toNanos(): Long = unit.toNanos(time)
    fun toMicros(): Long = unit.toMicros(time)
    fun toMillis(): Long = unit.toMillis(time)
    fun toSeconds(): Long = unit.toSeconds(time)
    fun toMinutes(): Long = unit.toMinutes(time)
    fun toHours(): Long = unit.toHours(time)
    fun toDays(): Long = unit.toDays(time)
}

public sealed class TimeType
public object Nanos : TimeType()
public object Micros : TimeType()
public object Millis : TimeType()
public object Seconds : TimeType()
public object Minutes : TimeType()
public object Hours : TimeType()
public object Days : TimeType()


/**
 * 将一个 [Time] 转化为一个指定的类型。
 */
public infix fun Time.timeAs(type: TimeType): Long {
    return when (type) {
        Nanos -> toNanos()
        Micros -> toMicros()
        Millis -> toMillis()
        Seconds -> toSeconds()
        Minutes -> toMinutes()
        Hours -> toHours()
        Days -> toDays()
    }
}

/**
 * 将一个时间数值根据 [时间类型][timeUnit] 转化为一个内联时间类 [Time]
 */
public infix fun Number.timeBy(timeUnit: TimeUnit): Time = Time(timeUnit.toMillis(toLong()))

/** 秒转毫秒 */
public fun Number.secondToMill(): Long = TimeUnit.SECONDS.toMillis(toLong())

@OptIn(ExperimentalTime::class)
public fun Long.secondToMill2(): Long = Duration.seconds(this).inWholeMilliseconds

/**
 * 获取当前时间的 [Time] 类型
 */
public fun now(): Time = Time(System.currentTimeMillis())




