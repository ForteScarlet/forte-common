package love.forte.common.utils.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * 这是一个标记性注解，当标记此注解时， {@link AnnotationUtil} 在解析一个 <b>可重复注解</b> 的时候，
 * 会尝试将正常的可重复注解与其他继承性质的注解进行合并。
 *
 * @author ForteScarlet
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({
        ElementType.TYPE,
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.CONSTRUCTOR,
        ElementType.LOCAL_VARIABLE,
        ElementType.ANNOTATION_TYPE,
        ElementType.PACKAGE,
        ElementType.TYPE_USE
}) // 使用在注解的方法上
public @interface MixRepeatableAnnotations {
}
