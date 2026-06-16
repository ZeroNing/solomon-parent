package com.steven.solomon.rabbitmq.config;

import cn.hutool.core.util.ObjectUtil;

import com.steven.solomon.rabbitmq.factory.RabbitMqTenantContext;
import com.steven.solomon.rabbitmq.properties.TenantRabbitMqProperties;
import com.steven.solomon.rabbitmq.service.AbstractMQService;
import com.steven.solomon.rabbitmq.service.DelayedMQService;
import com.steven.solomon.rabbitmq.service.DirectMQService;
import com.steven.solomon.rabbitmq.service.FanoutMQService;
import com.steven.solomon.rabbitmq.service.HeadersMQService;
import com.steven.solomon.rabbitmq.service.TopicMQService;
import com.steven.solomon.rabbitmq.init.RabbitMqTenantInitService;
import com.steven.solomon.rabbitmq.utils.RabbitUtils;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.Map;

/**
 * RabbitMQ 自动配置。
 *
 * <p>同时支持单租户和多租户两种部署模式：</p>
 * <ul>
 *   <li>单租户：沿用 Spring Boot 标准 {@code spring.rabbitmq.*} 配置，由
 *       {@link RabbitAutoConfiguration} 创建连接工厂。</li>
 *   <li>多租户：通过 {@code rabbitmq.tenant.<租户编码>} 为每个租户配置独立连接，
 *       由 {@link RabbitMqTenantInitService} 初始化并注册到 {@link RabbitMqTenantContext}。
 *       通过 {@code rabbitmq.tenant-mode} 切换每租户独立连接或共享连接。</li>
 * </ul>
 *
 * <p>统一注册消息转换器、发送模板、队列管理器和六种交换机类型的发送服务，
 * 业务侧直接注入 {@link AbstractMQService} 即可发送消息。</p>
 *
 * @author steven
 */
@Configuration
@EnableConfigurationProperties(value = {RabbitProperties.class, TenantRabbitMqProperties.class})
@Import(value = {RabbitUtils.class, DelayedMQService.class, DirectMQService.class,
        FanoutMQService.class, TopicMQService.class, HeadersMQService.class,
        RabbitAutoConfiguration.class, RabbitMqTenantContext.class})
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class RabbitConfig {

    private static final Logger logger = LoggerUtils.logger(RabbitConfig.class);

    private final ApplicationContext applicationContext;
    private final TenantRabbitMqProperties tenantRabbitMqProperties;
    private final RabbitMqTenantContext rabbitMqTenantContext;

    public RabbitConfig(ApplicationContext applicationContext,
                        TenantRabbitMqProperties tenantRabbitMqProperties,
                        RabbitMqTenantContext rabbitMqTenantContext) {
        // 缓存 Spring 上下文，便于后续通过 SpringUtil 获取 Bean
        SpringUtil.setContext(applicationContext);
        this.applicationContext = applicationContext;
        this.tenantRabbitMqProperties = tenantRabbitMqProperties;
        this.rabbitMqTenantContext = rabbitMqTenantContext;
    }

    /**
     * 容器启动后执行多租户连接初始化。
     *
     * <p>当配置了 {@code rabbitmq.tenant} 多租户连接信息时，按 {@code rabbitmq.tenant-mode}
     * 为每个租户（或共享）创建 RabbitMQ 连接工厂并注册到 {@link RabbitMqTenantContext}。</p>
     */
    @PostConstruct
    public void initTenantConnections() {
        // 仅当配置了多租户连接信息时才初始化，单租户模式跳过
        if (ObjectUtil.isNotEmpty(tenantRabbitMqProperties.getTenant())) {
            RabbitMqTenantInitService initService = new RabbitMqTenantInitService(
                    tenantRabbitMqProperties, rabbitMqTenantContext, applicationContext.getEnvironment());
            initService.init();
        }
    }

    /**
     * 消息转换器，统一将消息体序列化为 JSON。
     */
    @Bean("messageConverter")
    @ConditionalOnMissingBean(MessageConverter.class)
    @Conditional(RabbitCondition.class)
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitMQ 发送模板，业务侧通过它发送消息。
     *
     * <p>自动注入确认回调与返回回调（当业务定义了 {@link AbstractRabbitCallBack} 时），
     * 并按配置设置超时时间。</p>
     */
    @Bean("rabbitTemplate")
    @ConditionalOnMissingBean(RabbitTemplate.class)
    @Conditional(RabbitCondition.class)
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter,
                                         RabbitProperties properties) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        // mandatory=true 表示消息无法路由时返回给生产者而非丢弃
        rabbitTemplate.setMandatory(ObjectUtil.defaultIfNull(properties.getTemplate().getMandatory(), true));
        if (ObjectUtil.isNotEmpty(properties.getTemplate().getReceiveTimeout())) {
            rabbitTemplate.setReceiveTimeout(properties.getTemplate().getReceiveTimeout().toMillis());
        }
        if (ObjectUtil.isNotEmpty(properties.getTemplate().getReplyTimeout())) {
            rabbitTemplate.setReplyTimeout(properties.getTemplate().getReplyTimeout().toMillis());
        } else {
            // 未配置时默认 120 秒，避免 RPC 场景过早超时
            rabbitTemplate.setReplyTimeout(120000);
        }
        // 注入业务自定义的确认/返回回调
        Map<String, AbstractRabbitCallBack> callBackMap = SpringUtil.getBeansOfType(AbstractRabbitCallBack.class);
        if (ObjectUtil.isNotEmpty(callBackMap)) {
            RabbitCallBack rabbitCallBack = new RabbitCallBack(callBackMap.values());
            rabbitTemplate.setConfirmCallback(rabbitCallBack);
            rabbitTemplate.setReturnsCallback(rabbitCallBack);
        }
        return rabbitTemplate;
    }

    /**
     * 队列管理器，用于自动声明交换机、队列和绑定关系。
     */
    @Bean("rabbitAdmin")
    @ConditionalOnMissingBean(RabbitAdmin.class)
    @Conditional(RabbitCondition.class)
    @Primary
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        // 容器启动时自动声明 @Bean 形式的队列、交换机和绑定
        rabbitAdmin.setAutoStartup(true);
        logger.info("RabbitAdmin 初始化完成, 启动时将自动声明队列与交换机");
        return rabbitAdmin;
    }
}
