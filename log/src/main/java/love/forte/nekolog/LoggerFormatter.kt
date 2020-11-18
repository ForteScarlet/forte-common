/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     LoggerFormater.kt
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

@file:JvmName("NekoLoggers")

package love.forte.nekolog

import love.forte.nekolog.color.ColorBuilder
import love.forte.nekolog.color.ColorTypes
import love.forte.nekolog.color.FontColorTypes
import org.slf4j.event.Level
import java.text.MessageFormat
import java.time.LocalDateTime
import java.util.*
import java.util.regex.Pattern


public fun interface LoggerNameReset {
    fun resetLogName(name: String)
}

internal operator fun LoggerNameReset.invoke(name: String) { this.resetLogName(name) }


internal val LoggerNameNotSet = LoggerNameReset {  }



/**
 *
 * @author ForteScarlet -> https://github.com/ForteScarlet
 */
public interface LoggerFormatter {
    /**
     * 对输出的日志文本进行格式化。
     *
     * @param loggerNameReset 可以对logger的name进行重命名以降低名称计算压力。
     */
    fun format(info: FormatterInfo, loggerNameReset: LoggerNameReset): String

    @JvmDefault
    fun format(info: FormatterInfo): String = format(info, LoggerNameNotSet)

    /**
     * 只得到格式化的正文文本。
     */
    fun formatText(text: String?, args: Array<out Any?>): String
}


public data class FormatterInfo
@JvmOverloads
constructor(
    val msg: String? = null,
    val level: Level? = null,
    val name: String? = null,
    val thread: Thread? = null,
    val stackTrace: StackTraceElement? = null,
    val colorBuilder: ColorBuilder = ColorBuilder.getNocolorInstance(),
    var args: Array<out Any?>
) {


    companion object {
        @JvmStatic
        fun create(msg: String, level: Level, vararg args: Any?): FormatterInfo =
            FormatterInfo(msg = msg, level = level, args = args)
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        Thread.currentThread().stackTrace

        other as FormatterInfo

        if (msg != other.msg) return false
        if (level != other.level) return false
        if (colorBuilder != other.colorBuilder) return false
        if (!args.contentEquals(other.args)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = msg?.hashCode() ?: 0
        result = 31 * result + (level?.hashCode() ?: 0)
        result = 31 * result + (colorBuilder.hashCode())
        result = 31 * result + args.contentHashCode()
        return result
    }
}


public abstract class BaseLoggerFormatter(private val textFormat: (String?, Array<out Any?>) -> String) :
    LoggerFormatter {

    /**
     * 对输出的日志文本进行格式化。
     */
    override fun format(info: FormatterInfo, loggerNameReset: LoggerNameReset): String {
        val builder = info.colorBuilder
        // [time][threadName] [level] stackTrace : msg
        val level = info.level
        val defColor: ColorTypes = FontColorTypes.BLUE
        val color: ColorTypes = level?.color ?: defColor

        val nameColor = FontColorTypes.DARK_GREEN

        builder.color(defColor)

        builder.add("[", LocalDateTime.now().toString(), "]")
        info.thread?.apply {
            val threadName = this.name
            builder.add("[", threadName, "]")
        }
        builder.add("-")
        level?.apply {
            builder.add(defColor, "[")
            builder.add(color, this.name.text)
            builder.add(defColor, "] ")
            builder.append("| ")
        }
        info.name?.apply {
            val logName: String = this.toLogName()
            if (logName.length != this.length) {
                loggerNameReset(logName)
            }
            builder.add(nameColor, logName).append(' ')
        }
        info.stackTrace?.apply {
            if (info.name != null) {
                builder.append("| ")
            }
            val stackTraceText: String = this.show(info.name)
            builder.add(nameColor, stackTraceText).append(' ')
        }
        builder.append(": ")
        builder.append(textFormat(info.msg, info.args))
        return builder.toString()
    }

    override fun formatText(text: String?, args: Array<out Any?>): String = textFormat(text, args)
}


/**
 * 使用 Language 进行格式化.
 */
object LanguageLoggerFormatter :
    BaseLoggerFormatter({ text, args -> love.forte.common.language.Language.format(text, *args) })


/**
 * 不进行语言格式化.
 */
object NoLanguageLoggerFormatter : BaseLoggerFormatter({ text, args ->
    text?.let {
        if (args.isEmpty()) it else toMessageFormat(it, Locale.getDefault()).format(args)
    } ?: "null"
})


/**
 * 将文本转化为MessageFormat格式的文本。其中将兼容 {code {} }的格式。
 *
 * @param text 文本
 * @return [MessageFormat]
 */
private fun toMessageFormat(text: String, locale: Locale): MessageFormat {
    return if (text.contains("{}")) {
        simpleFormat(text, locale)
    } else {
        normalFormat(text, locale)
    }
}

/**
 * 切割正则
 */
private val SIMPLE_SPLIT_PATTERN: Pattern = Pattern.compile("\\{}")


/**
 * 将 `{}` 的格式转化为 `{number}` 的格式。
 *
 * @param text   文本
 * @param locale locale
 * @return [MessageFormat]
 */
private fun simpleFormat(text: String, locale: Locale): MessageFormat {
    val split = SIMPLE_SPLIT_PATTERN.split(text, -1)
    val sb = java.lang.StringBuilder()
    for (i in split.indices) {
        sb.append(split[i])
        if (i < split.size - 1) {
            sb.append('{').append(i).append('}')
        }
    }
    return normalFormat(sb.toString(), locale)
}

/**
 * 正常解析。
 *
 * @param text   文本
 * @param locale locale
 * @return [MessageFormat]
 */
private fun normalFormat(text: String, locale: Locale): MessageFormat {
    return MessageFormat(text, locale)
}




private const val lengthThreshold = 60


internal fun StackTraceElement.show(name: String? = null): String {
    return this.toString().let {
        if (name != null && it.startsWith(name)) {
            when {
                name == it -> ""
                it[name.length] == '.' -> it.substring(name.length + 1)
                else -> it.substring(name.length)
            }
        } else if (it.length < lengthThreshold) {
            it
        } else {
            val sb = StringBuilder(lengthThreshold)
            className.toSplitSimpleString(sb)
            sb.append('(').append(fileName).append(':').append(lineNumber).append(')')
            sb.toString()
        }
    }
}

internal fun <B : Appendable> String.toSplitSimpleString(buffer: B) {
    val split: List<String> = split(".")
    split.forEachIndexed { index, s ->
        when {
            index == 0 -> {
                buffer.append(s).append('.')
            }
            index < split.lastIndex -> {
                buffer.append(s.firstOrNull().toString()).append('.')
            }
            else -> buffer.append(s)
        }
    }
}


private val Level.color: ColorTypes
    get() {
        return when (this) {
            Level.ERROR -> FontColorTypes.RED
            Level.WARN -> FontColorTypes.YELLOW
            Level.INFO -> FontColorTypes.BLUE
            Level.DEBUG -> FontColorTypes.PURPLE
            Level.TRACE -> FontColorTypes.DARK_GREEN
        }
    }

private var maxSize = 5

private val String.text: String
    get() {
        val size = maxSize
        return when {
            length == size -> this
            length > size -> {
                maxSize = length
                val appendSize = size - length
                this + String(CharArray(appendSize) { ' ' })
            }
            else -> {
                val appendSize = size - length
                this + String(CharArray(appendSize) { ' ' })
            }
        }
    }


private const val MAX_LENGTH = 45

@Volatile
private var maxLength: Int = 15
    set(value) {
        field = if (value < MAX_LENGTH) {
            value
        } else {
            MAX_LENGTH
        }
    }

internal fun resetMaxLength(length: Int): Int {
    if (maxLength >= MAX_LENGTH) {
        return maxLength
    }

    if (length in maxLength + 1 until MAX_LENGTH) {
        maxLength = length
    }

    return maxLength
}


internal fun String.toLogName(): String {
    return this.filling(resetMaxLength(this.length))
}


/**
 * 如果填充到的长度小于string,
 */
internal fun String.filling(fillingLength: Int, reFill: Boolean = true): String {
    return when {
        length == fillingLength -> this
        length > fillingLength -> StringBuilder().apply { this@filling.toSplitSimpleString(this) }.toString().let {
            if (reFill) it.filling(fillingLength, false)
            else it
        }
        else -> {
            val leftover = fillingLength - length
            val charArray = CharArray(fillingLength) { i ->
                if (i < leftover) ' '
                else this[length - (fillingLength - i)]
            }
            String(charArray)
        }
    }

}




