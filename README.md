# forte-common

common是从simple-robot项目中独立出来的公共模块，

提供一些公共功能的模块，例如依赖注入模块、日志模块、国际化模块、注解工具等。

这些模块都是通用的、可以作为单独依赖所使用的，因此独立为一个单独的项目。

索引：

### [配置读取模块](./configuration)

该模块提供了针对不同类型的配置文件的读取与实体注入的标准接口，且内部默认实现了针对于`properties`与`yml`文件的读取与解析，
并默认通过`hutool`工具提供类型转化功能。

如果默认提供的实现无法满足希求，这其中所有的步骤均可自定义。

### [依赖注入模块](./ioc)

该模块提供一个简单的依赖注入功能，并依赖上述**configuration**模块实现配置信息自动注入功能。

### [国际化模块](./language)

该模块提供一个通过自动识别当前系统语言环境并加载对应语言文件以实现国际化语言功能。

### [轻量级日志模块](./log)

该模块提供了一个支持简易彩色日志与上述**language**国际化功能的轻量级日志框架。
其实现`lsf4j`标准，可以轻易的在其他相关日志框架间进行切换，且依赖上述**configuration**模块实现简易配置文件功能。

### [实用工具模块](./utils)

该模块或许是最有用的模块了。提供一些功能性的工具，
例如：

- ⭐ 可以实现注解的继承获取的注解工具和注解动态代理实例工具`AnnotationUtil`、`AnnotationProxyUtil`；

- 类型转化标准接口与实现相关的`Converter`；

- 类字段扫描工具`FieldUtil`

- 以及其他...



# 使用

### Maven


