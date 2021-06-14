package anno.test;

import love.forte.common.utils.annotation.AnnotateMapping;

import java.lang.annotation.*;

/**
 * @author ForteScarlet
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@AnnotateMapping(Listener.class)
@Listener(name = "ChildListener")
public @interface ChildListener {

    int age();


}
