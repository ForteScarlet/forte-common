@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package love.forte.common.converter


/**
 *
 * 针对于基础数据类型 [Byte][ByteConverter] [Short][ShortConverter] [Int][IntConverter] [Long][LongConverter]
 * [Double] [Float] [Boolean] [Char] 的转化器。
 *
 */
public sealed interface PrimitiveConverter<T> : Converter<T>


/**
 * 与数据数据类型中的数字相关内容有关的转化器.
 */
public sealed class PrimitiveNumberConverter<T : Number>(override val targetType: Class<T>) : NumberConverter<T>, PrimitiveConverter<T>


/**
 * [Byte] 转化器.
 */
public object ByteConverter : PrimitiveNumberConverter<Byte>(Byte::class.java) {
    fun convertToByte(original: Any): Byte {
        return when (original) {
            is Number -> original.toByte()
            is Boolean -> if (original) 1 else 0
            is Char -> original.code.toByte()
            is String -> original.toByte()
            else -> throwConversionException(original, original::class.java, targetType)
        }
    }

    override fun convert(original: Any): Byte = convertToByte(original)

}

/**
 * [Short] 转化器.
 */
public object ShortConverter : PrimitiveNumberConverter<Short>(Short::class.java) {
    fun convertToShort(original: Any): Short {
        return when (original) {
            is Number -> original.toShort()
            is Boolean -> if (original) 1 else 0
            is Char -> original.code.toShort()
            is String -> original.toShort()
            else -> throwConversionException(original, original::class.java, targetType)
        }
    }

    override fun convert(original: Any): Short = convertToShort(original)

}


/**
 * [Int]转化器.
 */
public object IntConverter : PrimitiveNumberConverter<Int>(Int::class.java) {
    fun convertToInt(original: Any): Int {
        return when (original) {
            is Number -> original.toInt()
            is Boolean -> if (original) 1 else 0
            is Char -> original.code
            is String -> original.toInt()
            else -> throwConversionException(original, original::class.java, targetType)
        }
    }

    override fun convert(original: Any): Int = convertToInt(original)
}

/**
 * [Long]转化器.
 */
public object LongConverter : PrimitiveNumberConverter<Long>(Long::class.java) {
    fun convertToLong(original: Any): Long {
        return when (original) {
            is Number -> original.toLong()
            is Boolean -> if (original) 1L else 0L
            is Char -> original.code.toLong()
            is String -> original.toLong()
            else -> throwConversionException(original, original::class.java, targetType)
        }
    }

    override fun convert(original: Any): Long = convertToLong(original)
}

/**
 * [Double]转化器.
 */
public object DoubleConverter : PrimitiveNumberConverter<Double>(Double::class.java) {
    fun convertToDouble(original: Any): Double {
        return when (original) {
            is Number -> original.toDouble()
            is Boolean -> if (original) 1.0 else 0.0
            is Char -> original.code.toDouble()
            is String -> original.toDouble()
            else -> throwConversionException(original, original::class.java, targetType)
        }
    }

    override fun convert(original: Any): Double = convertToDouble(original)
}

/**
 * [Float]转化器.
 */
public object FloatConverter : PrimitiveNumberConverter<Float>(Float::class.java) {
    fun convertToFloat(original: Any): Float {
        return when (original) {
            is Number -> original.toFloat()
            is Boolean -> if (original) 1f else 0f
            is Char -> original.code.toFloat()
            is String -> original.toFloat()
            else -> throwConversionException(original, original::class.java, targetType)
        }
    }

    override fun convert(original: Any): Float = convertToFloat(original)
}


/**
 * [Boolean]转化器.
 */
public object BooleanConverter : PrimitiveConverter<Boolean> {
    override val targetType: Class<Boolean>
        get() = Boolean::class.java

    fun convertToBoolean(original: Any): Boolean {
        return when (original) {
            is Number -> original != 0
            is Boolean -> original
            is Char -> {
                // '0' '1'
                // 'N' 'Y'
                // 'n' 'y'
                when (original) {
                    '0', 'n', 'N', 'x', 'X' -> false
                    '1', 'y', 'Y', 'o', 'O' -> true
                    else -> throwConversionException(original, original::class.java, targetType) {
                        "Char value in '0', 'n', 'N', 'x', 'X' is false, " +
                                "in '1', 'y', 'Y', 'o', 'O' is true."
                    }
                }
            }
            is String -> original.toBoolean()
            else -> throwConversionException(original, original::class.java, targetType)
        }
    }

    override fun convert(original: Any): Boolean = convertToBoolean(original)
}


/**
 * [Char]转化器.
 */
public object CharConverter : PrimitiveConverter<Char> {
    override val targetType: Class<Char>
        get() = Char::class.java

    fun convertToChar(original: Any): Char {
        return when (original) {
            is Number -> original.toChar()
            is Boolean -> if (original) '1' else '0'
            is Char -> original
            is String -> if (original.length == 1) original.first() else throwConversionException(original,
                original::class.java,
                targetType) {
                "Too long string."
            }
            else -> throwConversionException(original, original::class.java, targetType)
        }
    }

    override fun convert(original: Any): Char = convertToChar(original)
}