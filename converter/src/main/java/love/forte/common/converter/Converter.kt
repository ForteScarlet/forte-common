package love.forte.common.converter

import org.jetbrains.annotations.TestOnly


/**
 * 一个转化器。
 *
 * 转化器的任务很简单：将一个类型的对象，转化为另一个类型的对象。
 *
 */
public sealed interface Converter<T> {

    /**
     * 被转化内容的目标类型。就是要被转化成的类型。
     */
    val targetType: Class<T>


    /**
     *
     * 提供一个 [被转化目标][original].
     *
     * @throws ConversionException 当转化过程出错或者无法转化的时候.
     */
    fun convert(original: Any): T?
}



public interface ObjectConverter<T> : Converter<T>


@TestOnly
fun r(converter: Converter<*>) {

    when(converter) {
        is PrimitiveConverter<*> -> {}
        is NumberConverter<*> -> {}
        is ObjectConverter<*> -> {}
    }

}


