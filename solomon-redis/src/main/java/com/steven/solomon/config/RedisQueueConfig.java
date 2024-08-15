package com.steven.solomon.config;

import com.steven.solomon.annotation.RedisQueue;
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
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.Topic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(value={RedisProperties.class, TenantRedisProperties.class, CacheProfile.class, CacheProperties.class})
@Import(value = {RedisTenantContext.class, JacksonConfig.class})
public class RedisQueueConfig extends AbstractMessageLineRunner {

    private final Logger logger = LoggerUtils.logger(getClass());

    private final RedisTenantContext redisTenantContext;

    public RedisQueueConfig(ApplicationContext applicationContext, RedisTenantContext redisTenantContext) {
        SpringUtil.setContext(applicationContext);
        this.redisTenantContext = redisTenantContext;
    }

    @Override
    public void init(List<Object> clazzList) throws Exception {
        // 判断消费者队列是否存在
        if (ValidateUtils.isEmpty(clazzList)) {
            logger.debug("AbstractMessageLineRunner:没有RedisQueue消费者");
            return;
        }
        Map<String, RedisConnectionFactory> tenantFactoryMap = redisTenantContext.getFactoryMap();
        for (Map.Entry<String, RedisConnectionFactory> entry : tenantFactoryMap.entrySet()) {
            RedisConnectionFactory factory = entry.getValue();
            for (Object abstractConsumer : clazzList) {
                RedisQueue redisQueue = AnnotationUtils.findAnnotation(abstractConsumer.getClass(), RedisQueue.class);
                if (ValidateUtils.isEmpty(redisQueue) || ValidateUtils.isEmpty(redisQueue.topic())) {
                    continue;
                }
                String topicName = SpringUtil.getElValue(redisQueue.topic(), ValidateUtils.getElDefaultValue(redisQueue.topic()));
                Topic topic = ValidateUtils.equalsIgnoreCase(redisQueue.mode().toString(), TopicMode.CHANNEL.toString()) ? new ChannelTopic(topicName) : new PatternTopic(topicName);
                RedisMessageListenerContainer container = new RedisMessageListenerContainer();
                container.setConnectionFactory(factory);
                container.addMessageListener((MessageListener) abstractConsumer, topic);
                container.afterPropertiesSet();
                container.start();
            }
        }
    }

    @Override
    public List<Object> getQueueClazzList() {
        return new ArrayList<>(SpringUtil.getBeansWithAnnotation(RedisQueue.class).values());
    }
}
