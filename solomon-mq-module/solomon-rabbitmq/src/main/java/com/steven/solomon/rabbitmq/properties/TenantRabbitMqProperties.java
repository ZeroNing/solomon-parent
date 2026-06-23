package com.steven.solomon.rabbitmq.properties;

import com.steven.solomon.context.AbstractTenantProperties;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * RabbitMQ 多租户配置。
 *
 * <p>复用公共多租户配置基类 {@link AbstractTenantProperties}，支持通过
 * {@code rabbitmq.tenant.<租户编码>} 为每个租户配置独立的连接参数。
 * 与 MQTT 模块保持一致的多租户模型：</p>
 *
 * <ul>
 *   <li>{@code rabbitmq.tenant-mode=PER_TENANT_CONNECTION}（默认）：每个租户独立连接工厂。</li>
 *   <li>{@code rabbitmq.tenant-mode=SHARED_CONNECTION}：共享一个连接，按消息体 tenantCode 路由。</li>
 * </ul>
 *
 * <p>每个租户的配置类型为 Spring Boot 原生 {@link RabbitProperties}，
 * 因此可直接复用 {@code host/port/username/password/virtual-host} 等全部标准字段。</p>
 *
 * @author steven
 */
@ConfigurationProperties(prefix = "rabbitmq")
@Validated
public class TenantRabbitMqProperties extends AbstractTenantProperties<RabbitProperties> {
}
