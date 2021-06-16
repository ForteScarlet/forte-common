package test;

import love.forte.common.collections.SortedQueue;
import love.forte.common.utils.annotation.AnnotateMapping;
import love.forte.common.utils.annotation.AnnotationUtil;

import java.util.Comparator;

/**
 * @author ForteScarlet
 */
@MyAnnotation
public class Test {

    @org.junit.jupiter.api.Test
    public void testMain() {

        FatherAnnotation annotation = AnnotationUtil.getAnnotation(Test.class, FatherAnnotation.class);

        System.out.println("annotation = " + annotation);

        assert annotation.name().equals("forli");

    }
}
