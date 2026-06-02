package com.steven.solomon.clamav.config;

import com.steven.solomon.clamav.properties.ClamAvProperties;
import com.steven.solomon.clamav.utils.ClamAvUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.capybara.clamav.ClamavClient;

/**
 * ClamAV 病毒扫描自动配置类。
 * <p>
 * 自动装配 ClamAV 病毒扫描的配置属性和客户端工具类。
 * 从配置文件中读取 ClamAV 服务器地址、端口等信息，
 * 当启用扫描时创建 {@link ClamavClient} 并注入 {@link ClamAvUtils} Bean。
 * </p>
 *
 * @author 创建者
 */
@Configuration
@EnableConfigurationProperties(value = {ClamAvProperties.class})
public class ClamAvConfig {

    /** ClamAV 配置属性。 */
    private final ClamAvProperties clamAVProperties;

    /**
     * 通过构造器注入 ClamAV 配置属性。
     *
     * @param clamAVProperties ClamAV 配置属性
     */
    public ClamAvConfig(ClamAvProperties clamAVProperties) {
        this.clamAVProperties = clamAVProperties;
    }

    /**
     * 注册 ClamAV 病毒扫描工具 Bean。
     * <p>
     * 仅在容器中不存在 {@link ClamAvUtils} 类型的 Bean 时生效。
     * 如果配置启用 ClamAV，则创建 {@link ClamavClient} 连接到指定的服务器；
     * 否则创建无客户端的工具实例。
     * </p>
     *
     * @return {@link ClamAvUtils} 实例
     */
    @Bean
    @ConditionalOnMissingBean(ClamAvUtils.class)
    public ClamAvUtils clamAvUtils() {
        ClamavClient client = null;
        if (clamAVProperties.getEnabled()) {
            client = new ClamavClient(clamAVProperties.getHost(), clamAVProperties.getPort(), clamAVProperties.getPlatform());
        }
        return new ClamAvUtils(client, clamAVProperties);
    }
}
