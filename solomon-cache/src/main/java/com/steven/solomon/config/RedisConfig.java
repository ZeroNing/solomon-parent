package com.steven.solomon.config;

import com.steven.solomon.condition.RedisCondition;
import com.steven.solomon.enums.CacheModeEnum;
import com.steven.solomon.enums.CacheTypeEnum;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@DependsOn({"springUtil"})
public class RedisConfig extends CachingConfigurerSupport {

  private Logger logger = LoggerUtils.logger(getClass());

  @Resource
  private CacheProfile cacheProfile;

  @Value("${cache.type}")
  private String cacheType;

  @Resource
  private RedisTenantContext context;
  
  @PostConstruct
  public void afterPropertiesSet() {
    if(!ValidateUtils.equalsIgnoreCase(CacheTypeEnum.REDIS.toString(),cacheType)){
      return;
    }
    List<TenantRedisProperties> redisPropertiesList = new ArrayList<>();
    List<RedisClientPropertiesService> abstractRedisClientPropertiesServices = new ArrayList<>(SpringUtil.getBeansOfType(RedisClientPropertiesService.class).values());

//    if((ValidateUtils.isNotEmpty(cacheProfile) && CacheModeEnum.NORMAL.toString().equalsIgnoreCase(cacheProfile.getMode())) || (CacheModeEnum.TENANT_PREFIX.toString().equalsIgnoreCase(cacheProfile.getMode()))){
    RedisInitUtils.init(new TenantRedisProperties(cacheProfile.getRedisProfile()),context);
//    }

    abstractRedisClientPropertiesServices.forEach(service -> {
      redisPropertiesList.addAll(service.getRedisClient());
    });
    RedisInitUtils.init(redisPropertiesList,context);
  }

  @Bean(name = "redisTemplate")
  @Conditional(value = RedisCondition.class)
  public DynamicRedisTemplate dynamicMongoTemplate() {
    logger.info("?????????redis start");
    DynamicRedisTemplate<String, Object> redisTemplate = new DynamicRedisTemplate<String, Object>();
    // ???????????????
    RedisConnectionFactory factory = context.getFactoryMap().values().iterator().next();
    redisTemplate.setConnectionFactory(factory);
    // ??????Jackson2JsonRedisSerialize ?????????????????????
    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
    BaseRedisSerializer   baseRedisSerializer   = new BaseRedisSerializer();
    // key-value???????????????????????????
    redisTemplate.setKeySerializer(stringRedisSerializer);
    redisTemplate.setValueSerializer(baseRedisSerializer);
    // hash???????????????????????????,?????????????????????hash ????????????jdk????????????
    redisTemplate.setHashKeySerializer(stringRedisSerializer);
    redisTemplate.setHashValueSerializer(baseRedisSerializer);
    // ???????????????????????????
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


