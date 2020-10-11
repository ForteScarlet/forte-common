package love.forte.test;

import cn.hutool.core.io.resource.ResourceUtil;
import love.forte.common.configuration.Configuration;
import love.forte.common.configuration.ConfigurationInjector;
import love.forte.common.configuration.ConfigurationManagerRegistry;
import love.forte.common.configuration.ConfigurationParserManager;

import java.io.BufferedReader;

/**
 * @author <a href="https://github.com/ForteScarlet"> ForteScarlet </a>
 */
public class Test1 {


    public static void main(String[] args) throws Exception {
        // 读取配置
        final BufferedReader test1Reader = ResourceUtil.getUtf8Reader("test.yml");
        // 拿到一个默认的配置流解析器。
        final ConfigurationParserManager manager = ConfigurationManagerRegistry.defaultManager();
        // 作为yml类型进行解析。
        final Configuration configuration = manager.parse("yml", test1Reader);

        // configuration.get...

        // 配置注入器
        final ConfigurationInjector injector = ConfigurationInjector.INSTANCE;

        // 一个可以注入的配置类
        final TestConfig tc = new TestConfig();

        // 注入配置
        injector.inject(tc, configuration);

        System.err.println(tc);
        System.err.println(tc.getAge());
        System.err.println(tc.getPassword());

    }



}
