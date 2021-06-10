package love.forte.common.ioc.lifecycle

/**
 *
 * 此接口代表其实现类为一个 **生命周期** 实现。
 * 所有的 **生命周期** 实例最终均不会被依赖管理，而是通过构建其实例来达成某些目的。
 *
 * 这些实例应当都具有一个 **无参构造** 以保证其能够正常实例化。
 *
 * 这些实例有可能会被一次性使用并抛弃，也有可能会被记录在依赖中心中，这取决于他们的功能目的。
 *
 *
 *
 * @author ForteScarlet <ForteScarlet@163.com>
 * @date 2020/11/6
 * @since
 */
public interface BeanLifeCycle