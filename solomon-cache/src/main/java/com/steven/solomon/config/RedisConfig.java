package com.steven.solomon.config;

import com.steven.solomon.condition.RedisCondition;
import com.steven.solomon.enums.CacheTypeEnum;
import com.steven.solomon.init.RedisInitUtils;
import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.manager.DynamicDefaultRedisCacheWriter;
import com.steven.solomon.manager.SpringRedisAutoManager;
import com.steven.solomon.profile.TenantRedisProperties;
import com.steven.solomon.serializer.BaseRedisSerializer;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.template.DynamicRedisTemplate;
import com.steven.solomon.verification.ValidateUtils;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@DependsOn({"springUtil"})
public class RedisConfig extends CachingConfigurerSupport {

  private Logger logger = LoggerUtils.logger(getClass());

  @Resource
  private TenantRedisProperties properties;

  @Value("${cache.type}")
  private String cacheType;

  @Resource
  private RedisTenantContext context;
  
  @PostConstruct
  public void afterPropertiesSet() {
    if(!ValidateUtils.equalsIgnoreCase(CacheTypeEnum.REDIS.toString(),cacheType)){
      return;
    }
    RedisInitUtils.init(properties.getTenant(),context);
  }

  @Bean(name = "redisTemplate")
  @Conditional(value = RedisCondition.class)
  public DynamicRedisTemplate dynamicMongoTemplate() {
    logger.info("初始化redis start");
    DynamicRedisTemplate<String, Object> redisTemplate = new DynamicRedisTemplate<String, Object>();
    // 注入数据源
    RedisConnectionFactory factory = context.getFactoryMap().values().iterator().next();
    redisTemplate.setConnectionFactory(factory);
    // 使用Jackson2JsonRedisSerialize 替换默认序列化
    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
    BaseRedisSerializer   baseRedisSerializer   = new BaseRedisSerializer();
    // key-value结构序列化数据结构
    redisTemplate.setKeySerializer(stringRedisSerializer);
    redisTemplate.setValueSerializer(baseRedisSerializer);
    // hash数据结构序列化方式,必须这样否则存hash 就是基于jdk序列化的
    redisTemplate.setHashKeySerializer(stringRedisSerializer);
    redisTemplate.setHashValueSerializer(baseRedisSerializer);
    // 启用默认序列化方式
    redisTemplate.setEnableDefaultSerializer(true);
    redisTemplate.setEnableTransactionSupport(true);
    redisTemplate.afterPropertiesSet();
    return redisTemplate;
  }

  @Bean(name = "redisFactory")
  @Conditional(value = RedisCondition.class)
  public RedisConnectionFactory redisFactory() {
    return context.getFactoryMap().values().iterator().next();
  }

  @Bean
  @Override
  @Conditional(value = RedisCondition.class)
  public CacheManager cacheManager(){
    RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig().computePrefixWith((name -> name + ":"));
    SpringRedisAutoManager springRedisAutoManager = new SpringRedisAutoManager(DynamicDefaultRedisCacheWriter.nonLockingRedisCacheWriter(
        context.getFactoryMap().values().iterator().next()), defaultCacheConfig);
    return springRedisAutoManager;
  }
}


