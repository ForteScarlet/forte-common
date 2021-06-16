package test;

import java.lang.annotation.*;

/**
 * @author ForteScarlet
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface FatherAnnotation {

    String name();

}
