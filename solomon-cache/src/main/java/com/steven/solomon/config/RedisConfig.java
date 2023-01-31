package com.steven.solomon.config;

import com.steven.solomon.enums.CacheModeEnum;
import com.steven.solomon.init.RedisInitUtils;
import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.manager.DynamicDefaultRedisCacheWriter;
import com.steven.solomon.manager.SpringRedisAutoManager;
import com.steven.solomon.profile.CacheProfile;
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
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
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
  private RedisProperties redisProperties;

  @Resource
  private CacheProfile cacheProfile;
  
  @PostConstruct
  public void afterPropertiesSet() {
    List<TenantRedisProperties> redisPropertiesList = new ArrayList<>();
    List<RedisClientPropertiesService> abstractRedisClientPropertiesServices = new ArrayList<>(SpringUtil.getBeansOfType(RedisClientPropertiesService.class).values());

    if((ValidateUtils.isNotEmpty(cacheProfile) && CacheModeEnum.NORMAL.toString().equalsIgnoreCase(cacheProfile.getMode())) || (CacheModeEnum.TENANT_PREFIX.toString().equalsIgnoreCase(cacheProfile.getMode()))){
      RedisInitUtils.init(new TenantRedisProperties(redisProperties));
    }

    abstractRedisClientPropertiesServices.forEach(service -> {
      redisPropertiesList.addAll(service.getRedisClient());
    });
    RedisInitUtils.init(redisPropertiesList);
  }

  @Bean(name = "redisTemplate")
  public DynamicRedisTemplate dynamicMongoTemplate() {
    logger.info("初始化redis start");
    DynamicRedisTemplate<String, Object> redisTemplate = new DynamicRedisTemplate<String, Object>();
    // 注入数据源
    RedisConnectionFactory factory = RedisTenantContext.getFactoryMap().values().iterator().next();
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
  public RedisConnectionFactory redisFactory() {
    return RedisTenantContext.getFactoryMap().values().iterator().next();
  }

  @Bean
  public CacheManager cacheManager(){
    RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig().computePrefixWith((name -> name + ":"));
    SpringRedisAutoManager springRedisAutoManager = new SpringRedisAutoManager(DynamicDefaultRedisCacheWriter.nonLockingRedisCacheWriter(
        RedisTenantContext.getFactoryMap().values().iterator().next()), defaultCacheConfig);
    return springRedisAutoManager;
  }
}


