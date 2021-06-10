package love.forte.common.configuration.impl

import love.forte.common.configuration.Configuration
import love.forte.common.configuration.ConfigurationProperty


/**
 * Empty [Configuration]
 */
public object EmptyConfiguration : Configuration {
    override fun getConfig(key: String?): ConfigurationProperty? = null
    override fun setConfig(key: String?, config: ConfigurationProperty?): Nothing {
        throw IllegalStateException("This operation is not supported.")
    }
    override fun containsConfig(key: String?): Boolean = false
    override fun size(): Int = 0
}