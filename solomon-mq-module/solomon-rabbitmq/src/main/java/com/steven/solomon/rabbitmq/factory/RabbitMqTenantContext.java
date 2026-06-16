package com.steven.solomon.rabbitmq.factory;

import cn.hutool.core.util.ObjectUtil;

import com.steven.solomon.context.TenantContext;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;

/**
 * RabbitMQ 多租户连接上下文。
 *
 * <p>继承 {@link TenantContext}，维护"租户编码 → RabbitMQ 连接工厂"的映射，
 * 并通过 ThreadLocal 为当前请求/消息绑定对应租户的连接工厂。
 * 与数据源租户切换、Redis 租户切换保持一致的生命周期。</p>
 *
 * <p>使用方式：请求或消息进入时调用 {@link #setFactory(String)} 绑定当前租户，
 * 业务代码通过 {@link #getFactory()} 获取对应租户的连接工厂；结束时调用
 * {@link #removeFactory()} 清理，避免线程池复用导致租户串号。</p>
 *
 * @author steven
 */
public class RabbitMqTenantContext extends TenantContext<ConnectionFactory> {

    private static final Logger logger = LoggerUtils.logger(RabbitMqTenantContext.class);

    /**
     * 根据单个租户的 RabbitMQ 配置创建并缓存连接工厂。
     *
     * <p>使用 Spring AMQP 的 {@link CachingConnectionFactory} 包装底层连接，
     * 默认缓存通道，保证发送与消费性能。</p>
     *
     * @param tenantCode 租户编码
     * @param properties 该租户的 RabbitMQ 配置
     */
    public void registerFactory(String tenantCode, RabbitProperties properties) {
        if (ObjectUtil.isNull(properties)) {
            logger.warn("租户 {} 的 RabbitMQ 配置为空，跳过连接工厂创建", tenantCode);
            return;
        }
        CachingConnectionFactory factory = new CachingConnectionFactory(properties.getHost());
        factory.setPort(properties.getPort());
        factory.setUsername(properties.getUsername());
        factory.setPassword(properties.getPassword());
        if (ObjectUtil.isNotNull(properties.getVirtualHost())) {
            factory.setVirtualHost(properties.getVirtualHost());
        }
        // 启用心跳，避免长连接被中间件断开
        if (ObjectUtil.isNotNull(properties.getRequestedHeartbeat())) {
            factory.setRequestedHeartBeat((int) properties.getRequestedHeartbeat().getSeconds());
        }
        registerFactory(tenantCode, factory);
        logger.info("租户 {} 的 RabbitMQ 连接工厂创建成功, host={}, port={}",
                tenantCode, properties.getHost(), properties.getPort());
    }
}
