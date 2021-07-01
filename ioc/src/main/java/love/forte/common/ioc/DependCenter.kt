/*
 * Copyright (c) 2020. ForteScarlet All rights reserved.
 * Project  parent
 * File     DependCenter.kt
 *
 * You can contact the author through the following channels:
 * github https://github.com/ForteScarlet
 * gitee  https://gitee.com/ForteScarlet
 * email  ForteScarlet@163.com
 * QQ     1149159218
 */
@file:JvmName("DependCenters")
package love.forte.common.ioc

import love.forte.common.annotation.Ignore
import love.forte.common.configuration.Configuration
import love.forte.common.configuration.ConfigurationInjector
import love.forte.common.configuration.annotation.AsConfig
import love.forte.common.configuration.impl.EmptyConfiguration
import love.forte.common.configuration.impl.LinkedMapConfiguration
import love.forte.common.ioc.annotation.Beans
import love.forte.common.ioc.annotation.Constr
import love.forte.common.ioc.annotation.Depend
import love.forte.common.ioc.annotation.Pass
import love.forte.common.ioc.exception.*
import love.forte.common.ioc.lifecycle.AnnotationHelper
import love.forte.common.ioc.lifecycle.BeanDependRegistrar
import love.forte.common.ioc.lifecycle.BeanDependRegistry
import love.forte.common.ioc.lifecycle.CloseProcesses
import love.forte.common.utils.FieldUtil
import love.forte.common.utils.annotation.AnnotationUtil
import love.forte.common.utils.convert.ConverterManager
import java.io.Closeable
import java.lang.reflect.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiFunction
import java.util.function.Function
import kotlin.reflect.KMutableProperty
import kotlin.reflect.jvm.javaSetter
import kotlin.reflect.jvm.kotlinProperty

/**
 *
 * 依赖管理中心。
 *
 *
 *
 * @author ForteScarlet -> https://github.com/ForteScarlet
 *
 * @param singletonMap 保存单例的map。在put的时候会有同步锁，所以应该不需要线程安全的Map.
 * @param nameResourceWarehouse 保存依赖的map。以name为key, 对应着唯一的值，也是其他sourceWarehouse中最终指向的地方。
 * @param parent 依赖中心的父类依赖。可以通过 [mergeParent] 来指定一个新的 parent.
 * @param configuration 可以提供一个configuration来实现自动配置注入。默认为一个空的 [LinkedMapConfiguration]。不可为null。
 */
public class DependCenter
@JvmOverloads
constructor(
    private val singletonMap: MutableMap<String, Any> = mutableMapOf(),
    private val nameResourceWarehouse: MutableMap<String, BeanDepend<*>> = ConcurrentHashMap<String, BeanDepend<*>>(),
    @Volatile
    private var parent: DependBeanFactory? = null,
    var configuration: Configuration = LinkedMapConfiguration() // auto config able.
) : BeanDependRegistry, DependBeanFactory, Closeable {


    /**
     * 需要被初始化的beans列表
     */
    private var needInitialized: Queue<BeanDepend<*>>? = PriorityQueue { b1, b2 -> b1.priority.compareTo(b2.priority) }


    private var preInitPasses: Queue<DependPass>? = PriorityQueue { p1, p2 -> p1.priority.compareTo(p2.priority) }

    private var postInitPasses: Queue<DependPass>? = PriorityQueue { p1, p2 -> p1.priority.compareTo(p2.priority) }

    @Volatile
    private var initialized: Boolean = false

    @Volatile
    private var closed: Boolean = false

    private val initializedLock = Any()

    /**
     * do init.
     */
    @Synchronized
    public fun init() {
        if (!initialized) {
            synchronized(initializedLock) {
                // needInitialized.sortedBy { it.priority }

                preInitPasses?.let {  pre ->
                    while(pre.isNotEmpty()) {
                        pre.poll()(this)
                    }
                }

                needInitialized?.let { init ->
                    while (init.isNotEmpty()) {
                        init.poll().instanceSupplier(this)
                    }
                }

                postInitPasses?.let { post ->
                    while (post.isNotEmpty()) {
                        post.poll()(this)
                    }
                }

                preInitPasses = null
                needInitialized = null
                postInitPasses = null

                // while (preInitPasses?.isNotEmpty() == true) {
                //     preInitPasses?.poll()(this)
                // }

                // while (needInitialized?.isNotEmpty() == true) {
                //     needInitialized?.poll()?.instanceSupplier(this)
                // }

                // while (postInitPasses.isNotEmpty()) {
                //     postInitPasses.poll()(this)
                // }

            }

            initialized = true
        }
    }


    /**
     * close.
     * 会清除内部所有的东西，包括父类依赖工厂以及配置内容。
     * 此方法一般调用于 [shutdown hook][Runtime.addShutdownHook]
     */
    @Synchronized
    override fun close() {
        if (closed) {
            return
        }

        nameResourceWarehouse.values.forEach {
            if (it is CloseProcesses) {
                try {
                    it.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        nameResourceWarehouse.clear()
        singletonMap.clear()
        parent = null
        configuration = EmptyConfiguration

        preInitPasses = null


        closed = true
    }




    /**
     * 类型最终值。获取一个类型，先优先尝试使用此处。
     * value是可null类型，但是只适用于 [ConcurrentHashMap.computeIfAbsent]
     */
    private val finalTypeResourceWarehouse: MutableMap<Class<*>, String?> = ConcurrentHashMap<Class<*>, String?>()

    /**
     * 根据旧parent得到一个新的parent
     */
    @Synchronized
    public fun mergeParent(merger: (DependBeanFactory?) -> DependBeanFactory?) {
        parent = merger(parent)
    }

    public fun mergeConfig(merger: (Configuration) -> Configuration) {
        configuration = merger(configuration)
    }

    /**
     * 解析注解并注入依赖。 优先使用类上注解, 如果没有则使用提供的额外注解。 如果最终都没有, 抛出异常。
     *
     * @param defaultAnnotation 如果class有的class不存在bean, 可以提供一个默认的注解实例来代替为这个class的注解。
     * @param types 要注入的class列表。
     *
     * @throws NotBeansException 如果类上、方法上都找不到[Beans]相关注解，抛出此异常。
     */
    @JvmOverloads
    public fun inject(defaultAnnotation: Beans? = defaultBeansAnnotation, vararg types: Class<*>) {
        types.forEach {
            inject0(defaultAnnotation, it)
        }
    }


    /**
     * 解析注解并注入依赖。 优先使用类上注解, 如果没有则使用提供的额外注解。 如果最终都没有, 抛出异常。
     *
     * 会同时解析类中存在的 [love.forte.common.ioc.annotation.Pass]方法。
     *
     * @param defaultAnnotation 如果class有的class不存在bean, 可以提供一个默认的注解实例来代替为这个class的注解。
     * @param type 要注入的类型。
     *
     * @throws NotBeansException 如果类上、方法上都找不到[Beans]相关注解，抛出此异常。
     */
    private fun <T> inject0(defaultAnnotation: Beans? = defaultBeansAnnotation, type: Class<out T>) {
        // 自动解析的情况下, target type 不可以是抽象类型、接口类型、注解类型、枚举类型
        if (type.isInterface || Modifier.isAbstract(type.modifiers)) {
            throw IllegalTypeException("$type cannot be interface or abstract.")
        }
        if (type.isAnnotation || type.isEnum) {
            throw IllegalTypeException("$type cannot be annotation or enum type.")
        }

        // 如果是BeanDependRegistrar的实现类, 则会直接执行.
        if (BeanDependRegistrar::class.java.isAssignableFrom(type)) {
            val registrar: BeanDependRegistrar = type.newInstance() as BeanDependRegistrar
            registrar.registerBeanDepend(AnnotationHelper, this)
            return
        }

        // 得到beans注解
        val beansAnnotation: Beans =
            AnnotationUtil.getAnnotation(type, Beans::class.java) ?: defaultAnnotation ?: throw NotBeansException(
                type.toGenericString()
            )

        // builder
        val builder = BeanDependBuilder<T>()

        // bean name
        val beanDependName: String = beansAnnotation.value.let { if (it.isBlank()) null else it }
            ?: type.dependName

        builder.name(beanDependName)
            .type(type)
            .single(beansAnnotation.single)
            .needInit(beansAnnotation.init)
            .priority(beansAnnotation.priority)

        val asConfig: Boolean = AnnotationUtil.containsAnnotation(type, AsConfig::class.java)
        // 是否可以作为配置类
        builder.asConfig(asConfig)

        // 实例构建函数
        val emptyInstanceFunc: (DependBeanFactory) -> T = classToEmptyInstanceSupplier(type)

        // 值注入函数
        val instanceInject: (T, DependBeanFactory) -> T = instanceInjectFunc(beansAnnotation, type)

        val single: Boolean = beansAnnotation.single

        // 每次都会直接构造实例
        val realInstanceSupplier: InstanceSupplier<T> =
            InstanceSupplier { fac ->
                val emptyInstance: T = emptyInstanceFunc(fac)
                instanceInject(emptyInstance, fac)
                emptyInstance
            }

        // 完整实例构建函数
        val instanceSupplier: InstanceSupplier<T> = if (single) {
            InstanceSupplier { fac ->
                val singleton: Any? = singletonMap[beanDependName]
                if (singleton != null) {
                    singleton as T
                } else {
                    synchronized(singletonMap) {
                        val singleton0: Any? = singletonMap[beanDependName]
                        if (singleton0 == null) {
                            val instance: T = realInstanceSupplier(fac)
                            singletonMap[beanDependName] = instance as Any
                            instance
                        } else singleton0 as T
                    }
                }
            }
        } else realInstanceSupplier

        // 如果可以作为Config注入, 追加配置注入
        val instanceSupplierWithConfig: InstanceSupplier<T> = if (asConfig) {
            InstanceSupplier { fac ->
                val instance: T = instanceSupplier(fac)
                with(this.configuration) {
                    ConfigurationInjector.inject(instance, this, fac.getOrNull(ConverterManager::class.java))
                }
            }
        } else instanceSupplier

        builder.instanceSupplier(instanceSupplierWithConfig)

        // Register a beanDepend.
        val beanDepend: BeanDepend<T> = builder.build()
        register(beanDepend)

        injectChildren(beanDepend, defaultBeansAnnotation).forEach {
            register(it)
        }

        // register pass.
        registerPass(type)
    }

    /**
     * 父类型下所有的子方法注入
     */
    private fun injectChildren(parent: BeanDepend<*>, defaultAnnotations: Beans): Sequence<BeanDepend<*>> {
        // find children
        return parent.type.methods.asSequence()
            // 只允许存在@Beans的方法
            .filter { AnnotationUtil.containsAnnotation(it, Beans::class.java) }
            .map {
                val modifiers: Int = it.modifiers
                // 如果是static, 或者不是public方法
                if (Modifier.isStatic(modifiers)) {
                    throw IllegalTypeException("${Beans::class} cannot be annotated in static method.")
                }
                if (!Modifier.isPublic(modifiers)) {
                    throw IllegalTypeException("${Beans::class} cannot be annotated in no-public method.")
                }
                it
            }.map {
                val beansAnnotation: Beans = AnnotationUtil.getAnnotation(it, Beans::class.java) ?: defaultAnnotations
                // builder
                val builder = BeanDependBuilder<Any>()

                val returnType: Class<*> = it.returnType

                // bean name
                val beanDependName: String =
                    beansAnnotation.value.let { beanValue -> if (beanValue.isBlank()) null else beanValue }
                        ?: it.dependName

                builder.name(beanDependName)
                builder.type(returnType)
                builder.needInit(beansAnnotation.init)
                builder.single(beansAnnotation.single)
                builder.priority(beansAnnotation.priority)
                val asConfig: Boolean = AnnotationUtil.containsAnnotation(returnType, AsConfig::class.java)
                // 是否可以作为Config注入

                // 实例构建函数
                val emptyInstanceFunc: (DependBeanFactory) -> Any = childMethodToEmptyInstanceSupplier(parent, it)

                // 值注入函数
                val instanceInject: (Any, DependBeanFactory) -> Any = instanceInjectFunc(beansAnnotation, returnType)


                val single: Boolean = beansAnnotation.single

                // 每次都会直接构造实例
                val realInstanceSupplier: InstanceSupplier<Any> =
                    InstanceSupplier { fac ->
                        val instance: Any = emptyInstanceFunc(fac)
                        instanceInject(instance, fac)
                        instance
                    }

                // 完整实例构建函数
                val instanceSupplier: InstanceSupplier<Any> = if (single) {
                    InstanceSupplier { fac ->
                        singletonMap[beanDependName]
                            ?: synchronized(singletonMap) {
                                singletonMap[beanDependName]
                                    ?: run {
                                        val instance: Any = realInstanceSupplier(fac)
                                        singletonMap[beanDependName] = instance
                                        instance
                                    }
                            }
                    }
                } else realInstanceSupplier

                // 如果可以作为Config注入, 追加配置注入
                val instanceSupplierWithConfig: InstanceSupplier<Any> = if (asConfig) {
                    InstanceSupplier { fac ->
                        val instance: Any = instanceSupplier(fac)

                        with(this.configuration) {
                            ConfigurationInjector.inject(
                                instance,
                                this,
                                fac.getOrNull(ConverterManager::class.java)
                            )
                        }


                    }
                } else instanceSupplier

                // // 完整实例构建函数
                // val instanceSupplier: InstanceSupplier<*> = InstanceSupplier { fac ->
                //     instanceInject(emptyInstanceFunc(fac), fac)
                // }
                builder.instanceSupplier(instanceSupplierWithConfig)
                builder.build()
            }
    }


    /**
     * constr instance function.
     * @param constr Constructor<*>
     * @return (DependBeanFactory) -> T
     */
    private fun <T> constrInstance(constr: Constructor<*>) : (DependBeanFactory) -> T {
        if (constr.parameterCount > 0) {
            val parameterGetterList: List<(DependBeanFactory) -> Any?> = constr.parameters.mapIndexed { index, it ->
                val depend: Depend? = AnnotationUtil.getAnnotation(it, Depend::class.java)
                if (depend != null) {
                    val depType = depend.type
                    if (depend.type != Void::class.java) {
                        // use type
                        { fac -> fac.getOrThrow(depType.java) { e ->
                            InjectionFailedException("$it($index) in $constr by type '$depType'", e)
                        } }
                    } else {
                        // use name, if exists.
                        val name = depend.value
                        if (name.isNotBlank()) {
                            { fac -> fac.getOrThrow(name) { e ->
                                InjectionFailedException("$it($index) in $constr by name '$name'", e)
                            } }
                        } else {
                            val paramType = it.type
                            { fac -> fac.getOrThrow(paramType) { e ->
                                InjectionFailedException("$it($index) in $constr by type '$paramType'", e)
                            } }
                        }
                    }
                } else {
                    // use param type
                    val paramType = it.type
                    { fac -> fac.getOrThrow(paramType) { e ->
                        InjectionFailedException("$it($index) in $constr by type '$paramType'", e)
                    } }
                }
            }
            return { fac -> constr.newInstance(*Array(parameterGetterList.size) { i -> parameterGetterList[i](fac) }) as T }

        } else {
            return { _ -> constr.newInstance() as T }
        }
    }


    /**
     * 根据一个Class，解析并得到这个class的实例化函数。
     */
    private fun <T> classToEmptyInstanceSupplier(type: Class<out T>): (DependBeanFactory) -> T {

        val constrFuncs = type.declaredMethods.filter {
            val modifiers = it.modifiers
            // static, and no params. with @Constr
            Modifier.isStatic(modifiers) && it.parameterCount == 0 && (AnnotationUtil.getAnnotation(
                it,
                Constr::class.java
            ) != null)
        }


        when {
            // more @Constr
            constrFuncs.size > 1 -> {
                throw IllegalConstrException("More than 1 static method annotated by @Constr, but only need 1.")
            }
            // no @Constr
            constrFuncs.isEmpty() -> {
                val constructors = type.constructors

                if (constructors.size == 1) {
                    return constrInstance(constructors.first())
                } else {
                    // more than 1 constructors. find @Depend.
                    val constrListWithAnnotation = constructors.filter {
                        AnnotationUtil.getAnnotation(it, Depend::class.java) != null
                    }
                    when {
                        // more than 1 with @Depend.
                        constrListWithAnnotation.size > 1 -> throw IllegalConstrException("More than one constructor annotated by @Depend in $type. Must only one.")

                        // nothing.
                        constrListWithAnnotation.isEmpty() -> throw IllegalConstrException("Multiple constructor exists but no constructor annotated by @Depend in $type. Must need one.")

                        // only one.
                        else -> return constrInstance(constructors.first())
                    }
                }
            }
            // one @Constr
            else -> {
                val constrFunc = constrFuncs.first().apply { isAccessible = true }
                return { constrFunc.invoke(null) as T }
            }
        }
    }


    /**
     * 类中标注了@Beans的方法的实例构建函数。
     */
    private fun childMethodToEmptyInstanceSupplier(
        parent: BeanDepend<*>,
        method: Method
    ): (DependBeanFactory) -> Any {
        // val beans: Beans = AnnotationUtil.getAnnotation(method, Beans::class.java)
        val parentName: String = parent.name

        // 参数实例获取函数
        val parameters = method.parameters
        val parameterSupplierList: List<(DependBeanFactory) -> Any?> =
            parameters.mapIndexed { i, p ->
                // depend annotation.
                val depend: Depend? = AnnotationUtil.getAnnotation(p, Depend::class.java)
                val orIgnore: Boolean = depend?.orIgnore ?: false


                if (depend == null) {
                    val paramType = p.type

                    { d ->
                        d.getOrThrow(paramType) { e ->
                            NoSuchDependException(
                                """
                                |${e.localizedMessage}
                                |class:     ${method.declaringClass.toGenericString()}
                                |method:    ${method.toSimpleString()}
                                |parameter: ${p.toSimpleString(i)}
                            """.trimMargin()
                            )
                        }
                    }
                } else {
                    // depend.
                    val dependValue = depend.value
                    if (dependValue.isBlank()) {
                        // blank, use type.
                        val dependType: Class<*> =
                            depend.type.let { t -> if (t == Void::class) null else t.java } ?: p.type
                        if (orIgnore) {
                            { d -> d.getOrNull(dependType) }
                        } else {
                            { d ->
                                d.getOrThrow(dependType) { e ->
                                    NoSuchDependException(
                                        """
                                        |${e.localizedMessage}
                                        |class:     ${method.declaringClass.toGenericString()}
                                        |method:    ${method.toSimpleString()}
                                        |parameter: ${p.toSimpleString(i)}
                                    """.trimMargin()
                                    )
                                }
                            }
                        }
                    } else {
                        // not blank, use name.
                        val name = depend.value
                        if (orIgnore) {
                            { d -> d.getOrNull(name) }
                        } else {
                            { d ->
                                d.getOrThrow(name) { e ->
                                    NoSuchDependException(
                                        """
                                        |${e.localizedMessage}
                                        |class:     ${method.declaringClass.toGenericString()}
                                        |method:    ${method.toSimpleString()}
                                        |parameter: ${p.toSimpleString(i)}
                                    """.trimMargin()
                                    )
                                }

                            }
                        }
                    }
                }
            }


        return { factory ->
            // parent instance
            val parentInstance: Any = factory.getOrThrow(parentName) { e ->
                NoSuchDependException(
                    """
                    |Unable to get parent instance for injection method: ${e.localizedMessage}
                    |parent name: $parentName
                    |inject fun : ${method.toSimpleString()}
                """.trimMargin()
                )
            }
            // params
            val params = parameterSupplierList.map { sup -> sup(factory) }
            method(parentInstance, *params.toTypedArray())
        }
    }


    /**
     * 根据一个实例注入参数的函数.
     * 寻找其中全部标注了[Depend]的字段
     */
    private fun <T> instanceInjectFunc(beans: Beans, type: Class<out T>): (T, DependBeanFactory) -> T {
        val depends: MutableList<Field> = FieldUtil.getDeclaredFields(type, {
            if (beans.allDepend) {
                // 全部都注入, 则忽略部分需要忽略的
                !AnnotationUtil.containsAnnotation(it, Ignore::class.java)
            } else {
                // 否则, 注入需要注入的
                AnnotationUtil.containsAnnotation(it, Depend::class.java)
            }.apply {
                if (this) {
                    // 字段不可以是静态的、不可以是final的
                    val modifiers: Int = it.modifiers
                    if (Modifier.isFinal(modifiers)) {
                        throw IllegalTypeException("@Depend cannot be annotated in final field. but annotated in $it from $type")
                    }
                    if (Modifier.isStatic(modifiers)) {
                        throw IllegalTypeException("@Depend cannot be annotated in static field. but annotated in $it from $type")
                    }
                }
            }
        }, true)


        // println(type)

        return if (depends.isEmpty()) {
            // no depends.
            { b, _ -> b }
        } else {
            // contain depends.
            val funcs: List<(T, DependBeanFactory) -> Unit> = depends.map {
                it.isAccessible = true

                // do inject if can
                val dependAnnotation: Depend = AnnotationUtil.getAnnotation(it, Depend::class.java) ?: beans.depend

                val orIgnore: Boolean = dependAnnotation.orIgnore

                val dependName: String = dependAnnotation.value

                // 获取字段实例的方法
                val instance: (DependBeanFactory) -> Any? = if (dependName.isBlank()) {
                    // by type
                    val dependType: Class<*> = dependAnnotation.type.java
                    val needType: Class<*> = if (dependType != Void::class.java) {
                        dependType
                    } else {
                        it.type
                    }
                    if (orIgnore) { d ->
                        d.getOrNull(needType)
                    } else { d ->
                        d.getOrThrow(needType) { e ->
                            InjectionFailedException(
                                "depend($type) Injection failed for inject depend type $needType: ${e.localizedMessage}",
                                e
                            )
                        }
                    }
                } else {
                    if (orIgnore) { d ->
                        d.getOrNull(dependName)
                    } else { d ->
                        d.getOrThrow(dependName) { e ->
                            InjectionFailedException(
                                "depend($type) Injection failed for inject depend named $dependName: ${e.localizedMessage}",
                                e
                            )
                        }
                    }
                }


                // 通过field进行赋值
                fun byField(): (T, Any?) -> Unit = { b, v ->
                    v?.runCatching { it.set(b, this) }?.getOrElse { e ->
                        throw InjectionFailedException("field $it by $b for $v", e)
                    }
                }


                // 尝试通过setter赋值
                fun bySetter(): ((T, Any?) -> Unit)? {
                    val setterName: String = dependAnnotation.setterName


                    // by setter name.
                    if (setterName.isNotBlank()) {
                        val paramsType: Class<*> =
                            dependAnnotation.setterParams.java.takeIf { sp -> sp != Void::class.java } ?: it.type

                        val setter: Method = type.runCatching {
                            getMethod(setterName, paramsType)
                        }.getOrElse { e ->
                            throw IllegalStateException(
                                "Cannot find the setter method of ${type}: $setterName(${paramsType})",
                                e
                            );
                        }

                        return { b, v -> v?.apply { setter(b, this) } }
                    } else {
                        val ktProp: KMutableProperty<*> = it.kotlinProperty?.let { kp ->
                            if (kp is KMutableProperty) kp else null
                        } ?: return null

                        return ktProp.javaSetter?.let { setter ->
                            { b, v -> v?.apply { setter(b, this) } }
                        }
                    }

                }

                // 赋值函数, 提供一个载体与值，进行赋值
                val injector: (T, Any?) -> Unit = if (dependAnnotation.bySetter) {
                    // 优先通过setter获取, 得不到才用字段
                    bySetter() ?: byField()
                } else {
                    // 通过字段
                    byField()
                }

                { ins, fac ->
                    // 获取实例
                    val fieldValue: Any? = instance(fac)
                    // 注入
                    injector(ins, fieldValue)
                }
            }

            // return func
            ({ b, fac ->
                funcs.forEach { it(b, fac) }
                b
            })
        }
    }


    /**
     * 解析并注册一个类型下的pass。如果有的话。
     *
     * 只会解析 **public** **void** 的方法。
     */
    private fun registerPass(type: Class<*>) {
        // 寻找所有标注了 @Pass 注解的方法。
        type.declaredMethods.asSequence()
            .filter {
                AnnotationUtil.getAnnotation(it, Pass::class.java) != null
            }.map {
                val modifiers = it.modifiers
                // must be public.
                if (!Modifier.isPublic(modifiers)) {
                    throw IllegalStateException("@Pass must annotate in public method, but '$it' is not public.")
                }

                // cannot be static.
                if (Modifier.isStatic(modifiers)) {
                    throw IllegalStateException("@Pass cannot annotate static method, but '$it' is static.")
                }

                // return type must be void.
                val returnType: Class<*> = it.returnType
                if (returnType != Void.TYPE) {
                    throw IllegalStateException("@Pass annotated method's return type must be 'void', but '$it' is '$returnType'")
                }

                // to DependPass

                val passAnnotation: Pass = AnnotationUtil.getAnnotation(it, Pass::class.java)!!

                it.toDependPass(passAnnotation.priority) to passAnnotation
            }.forEach {
                registerPass(it.first, it.second)
            }

    }


    /**
     * 注册一个 [DependPass] 实例。
     */
    private fun registerPass(pass: DependPass, passAnnotation: Pass) {
        registerPass(pass, passAnnotation.preInit, passAnnotation.postInit)
    }


    /**
     * 注册一个 [DependPass] 实例。
     */
    private fun registerPass(pass: DependPass, pre: Boolean, post: Boolean) {
        if (pre) {
            registerPrePass(pass)
        }
        if (post) {
            registerPostPass(pass)
        }
    }


    /**
     * 注册一个pre pass
     */
    private fun registerPrePass(prePass: DependPass) {
        if (!initialized) {
            synchronized(initializedLock) {
                preInitPasses?.add(prePass)
            }
        } else {
            prePass(this)
        }
    }

    /**
     * 注册一个post pass
     */
    private fun registerPostPass(postPass: DependPass) {
        if (!initialized) {
            synchronized(initializedLock) {
                postInitPasses?.add(postPass)
            }
        } else {
            postPass(this)
        }
    }


    /**
     * 注册一个beanDepend. 直接注册。
     * 一切通过 [BeanDepend] 进行注册的实例均不会验证其内容，全部视为普通实例注册。
     *
     * @throws DuplicateDependNameException 如果name出现重复, 则可能抛出此异常
     */
    override fun register(beanDepend: BeanDepend<*>) {
        val name: String = beanDepend.name
        nameResourceWarehouse.merge(name, beanDepend, mergeDuplicate(name))

        // 需要init但是还没有init过
        if (beanDepend.needInit) {
            if (!initialized) {
                synchronized(initializedLock) {
                    needInitialized?.add(beanDepend)
                }
            } else {
                //init 过了, 直接获取一次
                beanDepend.instanceSupplier(this)
            }
        }


        // if (beanDepend.needInit && !initialized) {
        //
        // } else if (beanDepend.needInit) {
        //
        // }
    }

    /**
     * 注册一个type. 解析注解后注册
     * 如果这个type是[BeanDependRegistrar]的实现类, 则构建其实例并执行，而不注入到依赖中。
     */
    override fun register(type: Class<*>) {
        if (BeanDependRegistrar::class.java.isAssignableFrom(type)) {
            val registrar: BeanDependRegistrar = type.newInstance() as BeanDependRegistrar
            registrar.registerBeanDepend(AnnotationHelper, this)
        } else {
            inject(null, type)
        }
    }


    /**
     * 根据类型获取一个依赖实例。
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> getDepend(type: Class<out T>): BeanDepend<T>? {
        val name: String? = finalTypeResourceWarehouse.computeIfAbsent(type) {
            // find by all types.
            val depends: List<BeanDepend<*>> = nameResourceWarehouse.values
                .filter {
                    type.isAssignableFrom(it.type)
                }.sortedBy { it.priority }

            // nothing
            when {
                // empty
                depends.isEmpty() -> null
                // more than 1
                depends.size > 1 -> {
                    if (depends[0].priority == depends[1].priority) {
                        throw IllegalTypeException("Multiple depend($type) of the same priority: ${depends[0].priority}")
                    } else {
                        depends.first().name
                    }
                }
                else -> depends.first().name
            }
        }
        return name?.let { getDepend(it) as BeanDepend<T> }
    }


    /**
     * 根据名称获取一个依赖实例
     */
    private fun getDepend(name: String): BeanDepend<*>? = nameResourceWarehouse[name]


    /**
     * 尝试从已经实例化的单例中获取对应的类型。
     */
    private fun <T : Any?> getTypeFromSingleton(type: Class<T>): T? {
        return singletonMap.values.find { type.isInstance(it) } as? T
    }


    /**
     * 根据类型获取一个依赖实例。
     * @param type 类型
     * @throws NoSuchDependException 如果依赖没有找到则抛出异常
     * @return 实例
     */
    override fun <T : Any?> get(type: Class<T>): T {
        var parentException: Throwable? = null
        val parentValue: T? = try {
            parent?.get(type)
        } catch (e: Exception) {
            parentException = e
            null
        }
        return parentValue ?: getDepend(type)?.instanceSupplier?.invoke(this)
        ?: getTypeFromSingleton(type)
        ?: throw run {
            parentException?.let { NoSuchDependException(type.toString(), it) }
                ?: NoSuchDependException(type.toString())
        }
    }

    /**
     * 根据名称和类型获取一个依赖实例。通过名称获取，并转化为type。
     * @param type 类型
     * @param name 依赖名称
     * @throws NoSuchDependException 如果依赖没有找到则抛出异常
     * @return 转化后的实例
     */
    override fun <T : Any?> get(type: Class<T>, name: String): T {
        return get(name) as? T ?: throw NoSuchDependException(name)
    }

    /**
     * 根据名称获取一个依赖。
     * @param name 名称
     * @throws NoSuchDependException 如果依赖没有找到则抛出异常
     * @return 实例
     */
    override fun get(name: String): Any {
        var parentException: Throwable? = null
        val parentValue: Any? = try {
            parent?.get(name)
        } catch (e: Exception) {
            parentException = e
            null
        }
        return parentValue ?: getDepend(name)?.instanceSupplier?.invoke(this) ?: throw run {
            parentException?.let { NoSuchDependException(name, it) } ?: NoSuchDependException(name)
        }
    }

    /**
     * 根据类型获取一个依赖实例。获取不到则会返回null。
     * @param type 类型
     * @return 实例
     */
    override fun <T : Any?> getOrNull(type: Class<T>): T? {
        val parentValue: T? = try {
            parent?.get(type)
        } catch (e: Exception) {
            null
        }
        return parentValue ?: getDepend(type)?.instanceSupplier?.invoke(this)
    }

    /**
     * 根据名称和类型获取一个依赖实例。通过名称获取，并转化为type。获取不到则会返回null。
     * @param type 类型
     * @param name 依赖名称
     * @return 转化后的实例
     */
    override fun <T : Any?> getOrNull(type: Class<T>, name: String): T? {
        return getOrNull(name) as? T
    }

    /**
     * 根据名称获取一个依赖。获取不到则会返回null。
     * @param name 名称
     * @return 实例
     */
    override fun getOrNull(name: String): Any? {
        val parentValue: Any? = try {
            parent?.get(name)
        } catch (e: Exception) {
            null
        }
        return parentValue ?: getDepend(name)?.instanceSupplier?.invoke(this)
    }


    /**
     * 根据类型获取一个依赖实例。获取不到则会抛出对应异常。
     */
    override fun <T : Any?> getOrThrow(
        type: Class<T>,
        exceptionCompute: Function<NoSuchDependException, DependException>
    ): T {
        var parentEx: Exception? = null
        val parentValue: T? = try {
            parent?.get(type)
        } catch (e: Exception) {
            parentEx = e
            null
        }
        return parentValue ?: getDepend(type)?.instanceSupplier?.invoke(this)
        ?: throw exceptionCompute.apply(parentEx?.let { NoSuchDependException(type.name, it) } ?: NoSuchDependException(
            type.name
        ))
    }

    /**
     * 根据名称和类型获取一个依赖实例。通过名称获取，并转化为type。获取不到则会抛出对应异常。
     */
    override fun <T : Any?> getOrThrow(
        type: Class<T>,
        name: String,
        exceptionCompute: Function<NoSuchDependException, DependException>
    ): T = getOrThrow(name, exceptionCompute) as T

    /**
     * 根据名称获取一个依赖。获取不到则会抛出对应异常。
     */
    override fun getOrThrow(
        name: String,
        exceptionCompute: Function<NoSuchDependException, DependException>
    ): Any {
        var parentEx: Exception? = null
        val parentValue: Any? = try {
            parent?.get(name)
        } catch (e: Exception) {
            parentEx = e
            null
        }
        return parentValue ?: getDepend(name)?.instanceSupplier?.invoke(this)
        ?: throw exceptionCompute.apply(parentEx?.let { NoSuchDependException(name, it) }
            ?: NoSuchDependException(name))
    }

    /**
     * 获取所有beans的key。
     */
    override fun getAllBeans(): MutableSet<String> {
        return parent?.let {
            mutableSetOf<String>()
                .apply { addAll(it.allBeans) }
                .apply { addAll(nameResourceWarehouse.keys) }
        } ?: nameResourceWarehouse.keys.toMutableSet()

    }


    /**
     * 获取一个bean对应的类型。如果此bean存在的话。
     * 有时候可能不支持根据名称获取类型，
     * 因此请不要过于依赖此方法。
     * @param name bean
     * @return 对应类型
     */
    override fun getType(name: String?): Class<*>? =
        parent?.getType(name) ?: nameResourceWarehouse[name]?.type


    /**
     * 查询所有类型下子类型的结果
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getListByType(type: Class<T>): MutableCollection<out T> {
        // 如果为基类，不再过滤，返回所有结果
        return if (!type.isInterface && type == Any::class.java) {
            nameResourceWarehouse.values.map { it.instanceSupplier(this) } as MutableCollection<out T>
        } else {
            nameResourceWarehouse.values.asSequence().filter {
                type.isAssignableFrom(it.type)
            }.map { it.instanceSupplier(this) }.toMutableList() as MutableCollection<out T>
        }
    }

    /**
     * 查询所有的结果并作为Map返回。
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getListByTypeWithName(type: Class<T>): MutableMap<String, out T> {
        // 如果没有父类，则说明其为基类，不再过滤，返回所有结果
        return if (!type.isInterface && type == Any::class.java) {
            nameResourceWarehouse.map { (k, v) -> k to v.instanceSupplier(this) }.toMap()
                .toMutableMap() as MutableMap<String, out T>
        } else {
            nameResourceWarehouse.asSequence().filter { (_, v) ->
                type.isAssignableFrom(v.type)
            }.map { (k, v) -> k to v.instanceSupplier(this) }.toMap().toMutableMap() as MutableMap<String, out T>
        }
    }

    /**
     * companion object.
     */
    companion object {
        // default Beans annotation proxy instance.
        private val defaultBeansAnnotation: Beans = AnnotationUtil.getDefaultAnnotationProxy(Beans::class.java)
    }
}


/**
 * 将Method转化为 [DependPass] 实例。
 */
internal fun Method.toDependPass(priority: Int): DependPass = MethodDependPass(this, priority)


/**
 * 如果出现重复的name，抛出异常。
 */
internal fun <V> mergeDuplicate(name: String): BiFunction<in V, in V, out V> {
    return BiFunction { _, _ -> throw DuplicateDependNameException(name) }
}


/**
 * 根据类型得到依赖名称。
 * canonicalName ?: typeName
 */
internal val Class<*>.dependName: String
    get() {
        return this.canonicalName ?: this.typeName
    }

/**
 * 根据Method得到依赖名称。
 * declaringClass.propertyName
 */
internal val Method.dependName: String
    get() {
        val name: String = this.name
        return if (name.length > 3 && name.startsWith("get") && name[3].isUpperCase()) {
            val propertyName: String = name[3].toLowerCase() + name.substring(4)
            // class name + # + name
            "${declaringClass.dependName}#$propertyName"
        } else {
            "${declaringClass.dependName}#$name"
        }

    }


internal fun Method.toSimpleString(): String {
    val mod = this.modifiers
    return StringBuilder().append(
        when {
            Modifier.isPublic(mod) -> "public "
            Modifier.isPrivate(mod) -> "private "
            Modifier.isProtected(mod) -> "protected "
            else -> "default "
        }
    ).apply {
        if (Modifier.isFinal(mod)) {
            append("final ")
        }
    }.append(returnType.simpleName).append(" ")
        .append(name).also { sb ->
            parameters.joinTo(sb, ", ", "(", ")") {
                it.type.simpleName
            }
        }.toString()
}


internal fun Parameter.toSimpleString(index: Int = -1): String {
    return StringBuilder().also { sb ->
        if (index >= 0) sb.append(index).append(":")
    }.append(name).append(" ").append(type.simpleName).toString()
}


// internal fun <T> DependBeanFactory.getOrThrow(type: Class<T>, from: String): T =
//     runCatching {
//         this[type]
//     }.getOrElse { if (it is NoSuchDependException) throw NoSuchDependException(it.localizedMessage + " @ " + from) else throw it }
