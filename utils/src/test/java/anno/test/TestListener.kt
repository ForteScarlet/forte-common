/*
 *
 *  * Copyright (c) 2021. ForteScarlet All rights reserved.
 *  * Project  simple-robot
 *  * File     MiraiAvatar.kt
 *  *
 *  * You can contact the author through the following channels:
 *  * github https://github.com/ForteScarlet
 *  * gitee  https://gitee.com/ForteScarlet
 *  * email  ForteScarlet@163.com
 *  * QQ     1149159218
 *
 */

package anno.test

import love.forte.common.utils.annotation.AnnotationUtil
import kotlin.reflect.full.functions
import kotlin.reflect.jvm.javaMethod


/**
 *
 * @author ForteScarlet
 */
class TestListener {

    @Filters(value = [
        Filter(".h1", trim = true),
        Filter(".h2", trim = true),
        Filter(".h3", trim = true)],
        bots = ["2370606773"]
    )
    fun listen() {
    }

}

fun main() {
    val f = TestListener::class.functions.find { it.name == "listen" }!!
    val method = f.javaMethod!!

    println(method)
    val f1 = method.getAnnotation(Filters::class.java)
    println(f1)
    println("f1 bots: " + f1.bots.joinToString(", "))
    val f2 = AnnotationUtil.getAnnotation(method, Filters::class.java)
    println(f2)
    println("f2 bots: " + f2.bots.joinToString(", "))


}
