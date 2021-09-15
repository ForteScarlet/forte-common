package love.forte.common.converter

import java.math.BigInteger
import kotlin.reflect.KClass


/**
 * 数字相关的转化器. 除了 [PrimitiveNumberConverter] 中的数字相关以外，还有一些类似于 [java.math.BigInteger] 之类的内容。
 *
 *
 * @see kotlin.Number
 * @see java.lang.Number
 *
 */
public interface NumberConverter<T : Number> : Converter<T>


public abstract class BaseNumberConverter<T : Number>(override val targetType: Class<T>) : NumberConverter<T>


public object BigIntegerConverter : BaseNumberConverter<BigInteger>(BigInteger::class.java) {
    override fun convert(original: Any): BigInteger? {
        return when (original) {
            is BigInteger -> original
            is Number -> original.toString().toBigInteger()
            is Boolean -> (if (original) 1 else 0).toBigInteger()
            is Char -> original.code.toBigInteger()
            is String -> original.toBigInteger()
            else -> null
        }
    }
}

