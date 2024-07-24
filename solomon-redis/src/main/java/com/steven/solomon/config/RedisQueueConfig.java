package com.steven.solomon.config;

import com.steven.solomon.annotation.RedisQueue;
import com.steven.solomon.enums.TopicMode;
import com.steven.solomon.json.config.JacksonConfig;
import com.steven.solomon.pojo.enums.SwitchModeEnum;
import com.steven.solomon.profile.CacheProfile;
import com.steven.solomon.profile.TenantRedisProperties;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(value={RedisProperties.class, TenantRedisProperties.class, CacheProfile.class, CacheProperties.class})
@Import(value = {RedisTenantContext.class, JacksonConfig.class})
public class RedisQueueConfig implements CommandLineRunner {

    private final Logger logger = LoggerUtils.logger(getClass());
    private final RedisTenantContext redisTenantContext;

    private boolean isSwitchDb;

    private final CacheProfile cacheProfile;

    private RedisQueue redisQueue;

    public RedisQueueConfig(ApplicationContext applicationContext, CacheProfile cacheProfile, RedisTenantContext redisTenantContext){
        this.cacheProfile = cacheProfile;
        this.isSwitchDb         = ValidateUtils.equalsIgnoreCase(SwitchModeEnum.SWITCH_DB.toString(), cacheProfile.getMode().toString());
        SpringUtil.setContext(applicationContext);
        this.redisTenantContext = redisTenantContext;
    }

    @Override
    public void run(String... args) throws Exception {
        this.init(new ArrayList<>(SpringUtil.getBeansWithAnnotation(RedisQueue.class).values()));
    }

    /**
     * 初始化MQ
     *
     * @param clazzList 消费者集合数组
     */
    private void init(List<Object> clazzList) {
        // 判断消费者队列是否存在
        if (ValidateUtils.isEmpty(clazzList)) {
            logger.debug("MessageListenerConfig:没有RedisQueue消费者");
            return;
        }
        Map<String, RedisConnectionFactory> tenantFactoryMap = redisTenantContext.getFactoryMap();
        for(Map.Entry<String,RedisConnectionFactory> entry: tenantFactoryMap.entrySet()){
            RedisConnectionFactory factory = entry.getValue();
            for(Object abstractConsumer : clazzList){
                redisQueue = AnnotationUtils.findAnnotation(abstractConsumer.getClass(), RedisQueue.class);
                if(ValidateUtils.isEmpty(redisQueue)){
                    continue;
                }
                RedisMessageListenerContainer container = new RedisMessageListenerContainer();
                container.setConnectionFactory(factory);
                container.addMessageListener((MessageListener) abstractConsumer, ValidateUtils.equalsIgnoreCase(redisQueue.mode().toString(), TopicMode.CHANNEL.toString()) ? new ChannelTopic(redisQueue.topic()) : new PatternTopic(redisQueue.topic()));
                container.afterPropertiesSet();
                container.start();
            }
        }
    }
}
