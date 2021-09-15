package love.forte.common.converter


/**
 *
 * 当无法进行转化的时候
 *
 * @author ForteScarlet
 */
class ConversionException : RuntimeException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(
        message,
        cause,
        enableSuppression,
        writableStackTrace)
}


@Suppress("NOTHING_TO_INLINE")
public inline fun throwConversionException(original: Any, originalType: Class<*>, targetType: Class<*>): Nothing {
    throwConversionException { "Cannot convert $original (type: $originalType) to $targetType" }
}

public inline fun throwConversionException(original: Any, originalType: Class<*>, targetType: Class<*>, moreMessage: () -> String): Nothing {
    throwConversionException { "Cannot convert $original (type: $originalType) to $targetType : ${moreMessage()}" }
}

public inline fun throwConversionException(lazyMessage: () -> String): Nothing {
    throw ConversionException(lazyMessage())
}