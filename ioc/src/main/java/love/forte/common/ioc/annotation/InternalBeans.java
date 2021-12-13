package love.forte.common.ioc.annotation;

import kotlin.annotation.AnnotationRetention;
import kotlin.annotation.Retention;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * 标记一个类代表为“内部的”Bean，而并非使用者所定义的bean.
 * @author ForteScarlet
 */
@Retention(AnnotationRetention.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface InternalBeans {
}
