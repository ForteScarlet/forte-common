/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     CombinedDependBeanFactory.java
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */

package love.forte.common.ioc;

import love.forte.common.ioc.exception.DependException;
import love.forte.common.ioc.exception.NoSuchDependException;

import java.util.Set;
import java.util.function.Function;

/**
 *
 * 合并多个 {@link DependBeanFactory} 为一个。
 *
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
public class CombinedDependBeanFactory implements DependBeanFactory {

    /**
     * 根据类型获取一个依赖实例。
     *
     * @param type 类型
     * @return 实例
     * @throws NoSuchDependException 如果依赖没有找到则抛出异常。
     */
    @Override
    public <T> T get(Class<T> type) {
        return null;
    }

    /**
     * 根据名称和类型获取一个依赖实例。通过名称获取，并转化为type。
     *
     * @param type 类型
     * @param name 依赖名称
     * @return 转化后的实例
     * @throws NoSuchDependException 如果依赖没有找到则抛出异常
     */
    @Override
    public <T> T get(Class<T> type, String name) {
        return null;
    }

    /**
     * 根据名称获取一个依赖。
     *
     * @param name 名称
     * @return 实例
     * @throws NoSuchDependException 如果依赖没有找到则抛出异常
     */
    @Override
    public Object get(String name) {
        return null;
    }

    /**
     * 根据类型获取一个依赖实例。获取不到则会返回null。
     *
     * @param type 类型
     * @return 实例
     */
    @Override
    public <T> T getOrNull(Class<T> type) {
        return null;
    }

    /**
     * 根据名称和类型获取一个依赖实例。通过名称获取，并转化为type。获取不到则会返回null。
     *
     * @param type 类型
     * @param name 依赖名称
     * @return 转化后的实例
     */
    @Override
    public <T> T getOrNull(Class<T> type, String name) {
        return null;
    }

    /**
     * 根据名称获取一个依赖。获取不到则会返回null。
     *
     * @param name 名称
     * @return 实例
     */
    @Override
    public Object getOrNull(String name) {
        return null;
    }

    /**
     * 根据类型获取一个依赖实例。获取不到则会抛出 {@link NoSuchDependException} 异常。
     *
     * @param type             类型
     * @param exceptionCompute 获取一个异常。
     * @return 实例
     */
    @Override
    public <T> T getOrThrow(Class<T> type, Function<NoSuchDependException, DependException> exceptionCompute) {
        return null;
    }

    /**
     * 根据名称和类型获取一个依赖实例。通过名称获取，并转化为type。获取不到则会抛出 {@link NoSuchDependException} 异常。
     *
     * @param type             类型
     * @param name             依赖名称
     * @param exceptionCompute 获取一个异常。
     * @return 转化后的实例
     */
    @Override
    public <T> T getOrThrow(Class<T> type, String name, Function<NoSuchDependException, DependException> exceptionCompute) {
        return null;
    }

    /**
     * 根据名称获取一个依赖。获取不到则会抛出 {@link NoSuchDependException} 异常。
     *
     * @param name             名称
     * @param exceptionCompute 获取一个异常。
     * @return 实例
     */
    @Override
    public Object getOrThrow(String name, Function<NoSuchDependException, DependException> exceptionCompute) {
        return null;
    }

    /**
     * 获取目前已经存在的所有beans的name。
     * <p>
     * 有时候可能不支持获取所有名称，且有可能存在效率底下的问题，
     * 因此请不要过于依赖此方法。
     *
     * @return 结果集合。
     */
    @Override
    public Set<String> getAllBeans() {
        return null;
    }

    /**
     * 获取一个bean对应的类型。如果此bean存在的话。
     * 有时候可能不支持根据名称获取类型，
     * 因此请不要过于依赖此方法。
     *
     * @param name bean
     * @return 对应类型
     */
    @Override
    public Class<?> getType(String name) {
        return null;
    }
}
