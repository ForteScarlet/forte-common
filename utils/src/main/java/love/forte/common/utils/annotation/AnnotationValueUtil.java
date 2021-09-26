/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     AnnotationValueUtil.java
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

package love.forte.common.utils.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 操作注解值得工具类
 *
 * 需要注意的是，注解的实例仅存在一个，因此当你修改了注解的值，则全局生效。
 *
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
public class AnnotationValueUtil {

    private static final Map<Class<? extends Annotation>, Field> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取注解对应得value map。只有正常的注解才能够获取到。
     *
     * @param annotation 注解
     * @return value map
     * @see sun.reflect.annotation.AnnotationInvocationHandler#memberValues
     */
    @SuppressWarnings("JavadocReference")
    private static <T extends Annotation> Map<String, Object> getValueMap(T annotation) {
        if (annotation instanceof AnnotationInvocationHandler) {
            return ((AnnotationInvocationHandler) annotation).getMemberValuesMap();
        }

        InvocationHandler ih = Proxy.getInvocationHandler(annotation);

        if (ih instanceof AnnotationInvocationHandler) {
            return ((AnnotationInvocationHandler) ih).getMemberValuesMap();
        }

        throw new IllegalStateException("Only Annotation instances represented by love.forte.common.utils.annotation.AnnotationInvocationHandler can get dynamic ValueMap.");


    }

    /**
     * 修改Annotation的值
     *
     * @param annotation       注解
     * @param valueMapConsumer 注解的值
     */
    @org.jetbrains.annotations.Contract(pure = true)
    public static <T extends Annotation> T setValue(T annotation, Consumer<Map<String, Object>> valueMapConsumer) {
        T checkedAnnotation = checkAnnotationProxy(annotation);
        Map<String, Object> valueMap = getValueMap(checkedAnnotation);
        valueMapConsumer.accept(valueMap);
        return checkedAnnotation;
    }

    /**
     * 修改Annotation的值
     *
     * @param annotation 注解
     * @param key        注解的key
     * @param value      要修改的值
     */
    @org.jetbrains.annotations.Contract(pure = true)
    public static <T extends Annotation> T setValue(T annotation, String key, Object value) {
        T checkedAnnotation = checkAnnotationProxy(annotation);
        getValueMap(checkedAnnotation).put(key, value);
        return checkedAnnotation;
    }


    /**
     * 如果这是一个普通的annotation，将其转化为经由 {@link AnnotationInvocationHandler} 代理的annotation。
     *
     * @param annotation 注解实例
     */
    static <T extends Annotation> T checkAnnotationProxy(T annotation) {
        if (annotation instanceof AnnotationInvocationHandler) {
            return annotation;
        }

        InvocationHandler ih = Proxy.getInvocationHandler(annotation);

        if (ih instanceof AnnotationInvocationHandler) {
            return annotation;
        }

        Annotation proxyAnnotation = AnnotationProxyUtil.proxy(annotation.annotationType(), annotation, new LinkedHashMap<>());

        //noinspection unchecked
        return (T) proxyAnnotation;
    }

}

