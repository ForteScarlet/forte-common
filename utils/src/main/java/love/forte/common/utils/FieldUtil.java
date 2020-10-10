/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     FieldUtil.java
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

package love.forte.common.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 获取字段的工具类
 *
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
public class FieldUtil {

    /**
     * 获取某个类的所有字段
     * @param type 类
     * @param predicate 过滤器
     * @param withSuper 是否获取父类的字段
     * @return 字段列表
     */
    public static List<Field> getDeclaredFields(Class<?> type, Predicate<Field> predicate, boolean withSuper) {
        List<Field> list = new ArrayList<>();
        if (!withSuper) {
            final Field[] declaredFields = type.getDeclaredFields();
            for (Field field : declaredFields) {
                if (predicate.test(field)) {
                    list.add(field);
                }
            }
            return list;
        } else {
            return getDeclaredFields(type, list, predicate);
        }
    }

    /**
     * 获取某个类的所有字段
     * @param type 类
     * @param withSuper 是否获取父类的字段
     * @return 字段列表
     */
    public static List<Field> getDeclaredFields(Class<?> type, boolean withSuper) {
        List<Field> list = new ArrayList<>();
        if (!withSuper) {
            final Field[] declaredFields = type.getDeclaredFields();
            list.addAll(Arrays.asList(declaredFields));
            return list;
        } else {
            return getDeclaredFields(type, list, f -> true);
        }
    }


    private static List<Field> getDeclaredFields(Class<?> type, List<Field> fieldList, Predicate<Field> predicate) {
        final Field[] declaredFields = type.getDeclaredFields();
        for (Field field : declaredFields) {
            if (predicate.test(field)) {
                fieldList.add(field);
            }
        }
        final Class<?> superClass = type.getSuperclass();
        if (superClass != null && superClass != Object.class) {
            return getDeclaredFields(superClass, fieldList, predicate);
        } else {
            return fieldList;
        }
    }


    /**
     * 提供一个目标列表，和一个查询类型，查找这个类型中实现了的目标列表中的接口数量。
     * 例如：
     * {@code class A implements B, C}，
     * {@code interface B extends D}，
     *
     * 则可以认为 {@code A} 命中了 {@code B, C, D}。
     *
     * @param type 查找目标。
     * @param targets 寻找的目标接口。类型应当都是接口类型，否则没有意义。
     * @return
     */
    public static Set<Class<?>> getHitSuperInterfaces(Class<?> type, Set<Class<?>> targets) {
        return allInterfacesStream(type).distinct()
                // 寻找所有targets中的子类型。
                .flatMap(c -> targets.stream().filter(tc -> tc.isAssignableFrom(c)))
                .collect(Collectors.toSet());
    }

    /**
     * 提供一个目标列表，和一个查询类型，查找这个类型中实现了的目标列表中的接口数量。
     * 例如：
     * {@code class A implements B, C}，
     * {@code interface B extends D}，
     *
     * 则可以认为 {@code A} 命中了 {@code B, C, D}。
     *
     * 会出现重复的情况，需要注意去重。
     *
     * @param type 查找目标。
     * @param targets 寻找的目标接口。类型应当都是接口类型，否则没有意义。
     */
    public static Stream<Class<?>> getHitSuperInterfacesStream(Class<?> type, Set<Class<?>> targets) {
        return allInterfacesStream(type).distinct()
                // 寻找所有targets中的子类型。
                .flatMap(c -> targets.stream().filter(tc -> tc.isAssignableFrom(c)));
    }


    /**
     * 获取一个类型所实现的所有接口。包括诸如父类等层级关系。
     * @param type 某类型
     * @return 所有接口。
     */
    public static Set<Class<?>> allInterfaces(Class<?> type) {
        return allInterfacesStream(type).collect(Collectors.toSet());
    }


    /**
     * 获取一个类型所实现的所有接口。包括诸如父类等层级关系。
     *
     * 会出现重复的情况，需要注意去重。
     *
     * @param type 某类型
     * @return 所有接口。
     */
    public static Stream<Class<?>> allInterfacesStream(Class<?> type) {
        final Class<?>[] interfaces = type.getInterfaces();
        if(interfaces.length == 0) {
            return Stream.empty();
        }
        return Arrays.stream(interfaces).flatMap(FieldUtil::allInterfacesStream);
    }




}
