package test;

import love.forte.common.utils.annotation.AnnotateMapping;

import java.lang.annotation.*;

/**
 * @author ForteScarlet
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@FatherAnnotation(name = "forte")
@AnnotateMapping(FatherAnnotation.class)
public @interface MyAnnotation {

    String name() default "forli";

}
