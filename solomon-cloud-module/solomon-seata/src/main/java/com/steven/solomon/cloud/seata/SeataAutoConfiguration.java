package com.steven.solomon.cloud.seata;

import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Seata 分布式事务自动配置。
 *
 * <p>当 classpath 存在 Seata 且 {@code seata.enabled=true}（默认开启）时生效。
 * Seata 的 {@code @GlobalTransactional} 注解、数据源代理（AT 模式）由
 * seata-spring-boot-starter 自动装配，业务侧只需在配置中心指定 TC 地址即可。</p>
 *
 * <p><b>与多租户数据源的协同</b>：solomon-persistence 使用动态路由数据源
 * （{@code DynamicRoutingDataSource}），Seata 的 AT 模式需要代理真实数据源。
 * 二者协同方式：Seata 代理每个租户的真实数据源（在 {@code DynamicDataSourceFactory}
 * 创建时包装为 {@code DataSourceProxy}），动态路由层在最外层按租户选择已代理的数据源。
 * 这样全局事务分支提交与租户路由互不干扰。</p>
 *
 * @author steven
 */
@AutoConfiguration
@ConditionalOnClass(name = "io.seata.spring.annotation.GlobalTransactional")
@ConditionalOnProperty(name = "seata.enabled", havingValue = "true", matchIfMissing = true)
public class SeataAutoConfiguration {

    private static final Logger logger = LoggerUtils.logger(SeataAutoConfiguration.class);

    public SeataAutoConfiguration() {
        logger.info("Seata 分布式事务模块已启用，业务侧使用 @GlobalTransactional 开启全局事务");
    }
}
