# common-utils

提供一些通用的utils的模块。

## 注解的继承与映射

当一个注解标注在另一个注解之上的时候，AnnotationUtil视其为**注解继承**。

例如：

```java
public @interface A{}

@A
public @interface B{}
```

则视为 B继承了A。当然，多层继承也适用，例如：
```java
import love.forte.common.utils.annotation.AnnotationUtil;

public @interface A{}
@A
public @interface B{}

@B
public @interface C{}

public class Test {
    public static void main(String[] args){
        // 从B.class中拿到@A注解
        A a1 = AnnotationUtil.getAnnotation(B.class, A.class);
        // 从C.class中拿到@A注解
        A a2 = AnnotationUtil.getAnnotation(C.class, A.class);
    }   
}
```

上述代码中，`a1`和`a2`均可以得到一个对应的`@A`注解实例。




