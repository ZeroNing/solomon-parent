package com.steven.solomon.rabbitmq.init;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import com.steven.solomon.context.AbstractTenantProperties;
import com.steven.solomon.rabbitmq.factory.RabbitMqTenantContext;
import com.steven.solomon.rabbitmq.properties.TenantRabbitMqProperties;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.core.env.Environment;

/**
 * RabbitMQ 多租户连接初始化服务。
 *
 * <p>与 MQTT 模块保持一致的多租户连接策略，通过 {@code rabbitmq.tenant-mode} 切换：</p>
 * <ul>
 *   <li>{@code PER_TENANT_CONNECTION}（默认）：遍历 {@code rabbitmq.tenant}，
 *       为每个租户创建独立的 RabbitMQ 连接工厂并注册到 {@link RabbitMqTenantContext}。</li>
 *   <li>{@code SHARED_CONNECTION}：只取一个租户配置（优先 {@code default}）创建共享连接工厂，
 *       所有租户共用，消费时按消息体 tenantCode 路由。</li>
 * </ul>
 *
 * @author steven
 */
public class RabbitMqTenantInitService {

    private static final Logger logger = LoggerUtils.logger(RabbitMqTenantInitService.class);

    /** 共享连接模式下使用的逻辑租户编码。 */
    private static final String SHARED_TENANT_CODE = "SHARED";

    /** SHARED 模式下优先选取的默认租户配置键名。 */
    private static final String DEFAULT_TENANT_KEY = "default";

    /** 多租户模式配置项键名。 */
    private static final String TENANT_MODE_PROPERTY = "rabbitmq.tenant-mode";

    private final TenantRabbitMqProperties profile;
    private final RabbitMqTenantContext context;
    private final Environment environment;

    public RabbitMqTenantInitService(TenantRabbitMqProperties profile,
                                     RabbitMqTenantContext context,
                                     Environment environment) {
        this.profile = profile;
        this.context = context;
        this.environment = environment;
    }

    /**
     * 根据多租户模式初始化 RabbitMQ 连接工厂。
     *
     * <p>未启用或未配置租户连接信息时跳过初始化，避免无意义连接创建。</p>
     */
    public void init() {
        if (!profile.getEnabled()) {
            logger.info("RabbitMQ 未启用，跳过多租户连接初始化");
            return;
        }
        Map<String, RabbitProperties> tenantMap = profile.getTenant();
        if (ObjectUtil.isEmpty(tenantMap)) {
            logger.warn("未配置 RabbitMQ 租户连接信息，跳过多租户连接初始化");
            return;
        }
        String tenantMode = environment.getProperty(TENANT_MODE_PROPERTY);
        boolean shared = StrUtil.isNotBlank(tenantMode)
                && "SHARED_CONNECTION".equalsIgnoreCase(tenantMode.trim());
        logger.info("RabbitMQ 多租户模式: {}", shared ? "SHARED_CONNECTION" : "PER_TENANT_CONNECTION");
        if (shared) {
            // 共享连接模式：只创建一个连接工厂
            RabbitProperties sharedProps = tenantMap.getOrDefault(DEFAULT_TENANT_KEY,
                    tenantMap.values().iterator().next());
            logger.info("共享连接模式：创建单一 RabbitMQ 连接工厂，逻辑租户编码={}", SHARED_TENANT_CODE);
            context.registerFactory(SHARED_TENANT_CODE, sharedProps);
        } else {
            // 默认逐租户模式：为每个租户创建独立连接工厂
            for (Map.Entry<String, RabbitProperties> entry : tenantMap.entrySet()) {
                context.registerFactory(entry.getKey(), entry.getValue());
            }
        }
    }
}
