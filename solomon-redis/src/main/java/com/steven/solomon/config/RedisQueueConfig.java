package com.steven.solomon.config;

import cn.hutool.core.annotation.AnnotationUtil;
import com.steven.solomon.annotation.MessageListener;
import com.steven.solomon.enums.TopicMode;
import com.steven.solomon.init.AbstractMessageLineRunner;
import com.steven.solomon.json.config.JacksonConfig;
import com.steven.solomon.profile.CacheProfile;
import com.steven.solomon.profile.TenantRedisProperties;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;

import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(value={RedisProperties.class, TenantRedisProperties.class, CacheProfile.class, CacheProperties.class})
@Import(value = {RedisTenantContext.class, JacksonConfig.class})
public class RedisQueueConfig extends AbstractMessageLineRunner<MessageListener> {

    private final Logger logger = LoggerUtils.logger(getClass());

    private final RedisTenantContext redisTenantContext;

    public RedisQueueConfig(ApplicationContext applicationContext, RedisTenantContext redisTenantContext) {
        SpringUtil.setContext(applicationContext);
        this.redisTenantContext = redisTenantContext;
    }

    @Override
    public void init(List<Object> clazzList) throws Exception {
        Map<String, RedisConnectionFactory> tenantFactoryMap = redisTenantContext.getFactoryMap();
        for (Map.Entry<String, RedisConnectionFactory> entry : tenantFactoryMap.entrySet()) {
            RedisConnectionFactory factory = entry.getValue();
            for (Object abstractConsumer : clazzList) {
                MessageListener messageListener = AnnotationUtil.getAnnotation(abstractConsumer.getClass(), MessageListener.class);
                if (ValidateUtils.isEmpty(messageListener) || ValidateUtils.isEmpty(messageListener.topic())) {
                    continue;
                }
                String topicName = SpringUtil.getElValue(messageListener.topic(), ValidateUtils.getElDefaultValue(messageListener.topic()));
                Topic topic = ValidateUtils.equalsIgnoreCase(messageListener.mode().toString(), TopicMode.CHANNEL.toString()) ? new ChannelTopic(topicName) : new PatternTopic(topicName);
                RedisMessageListenerContainer container = new RedisMessageListenerContainer();
                container.setConnectionFactory(factory);
                container.addMessageListener((org.springframework.data.redis.connection.MessageListener) abstractConsumer, topic);
                container.afterPropertiesSet();
                container.start();
            }
        }
    }
}
