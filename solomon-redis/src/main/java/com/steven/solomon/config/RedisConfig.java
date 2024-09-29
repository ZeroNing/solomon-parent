package com.steven.solomon.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.steven.solomon.code.BaseCode;
import com.steven.solomon.init.RedisInitUtils;
import com.steven.solomon.json.config.JacksonConfig;
import com.steven.solomon.manager.DynamicDefaultRedisCacheWriter;
import com.steven.solomon.manager.SpringRedisAutoManager;
import com.steven.solomon.pojo.enums.SwitchModeEnum;
import com.steven.solomon.profile.CacheProfile;
import com.steven.solomon.profile.TenantRedisProperties;
import com.steven.solomon.service.ICacheService;
import com.steven.solomon.service.impl.RedisService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.template.DynamicRedisTemplate;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(value={RedisProperties.class,TenantRedisProperties.class, CacheProfile.class,CacheProperties.class})
@Import(value = {RedisTenantContext.class,JacksonConfig.class})
public class RedisConfig extends CachingConfigurerSupport {

  private final Logger logger = LoggerUtils.logger(getClass());

  private final TenantRedisProperties properties;

  private final RedisTenantContext context;

  private final boolean isSwitchDb;

  private final RedisProperties redisProperties;

  private final CacheProfile cacheProfile;

  private final ObjectMapper objectMapper;


  public RedisConfig(TenantRedisProperties properties, RedisTenantContext context, RedisProperties redisProperties,
                     ApplicationContext applicationContext, CacheProfile cacheProfile, ObjectMapper objectMapper) {
    this.properties         = properties;
    this.context            = context;
    this.isSwitchDb         = ValidateUtils.equalsIgnoreCase(SwitchModeEnum.SWITCH_DB.toString(), cacheProfile.getMode().toString());
    this.redisProperties    = redisProperties;
    this.cacheProfile       = cacheProfile;
    this.objectMapper = objectMapper;
    SpringUtil.setContext(applicationContext);
  }

  @PostConstruct
  public void afterPropertiesSet() {
    logger.info("Redis当前模式为:{}", cacheProfile.getMode().getDesc());
    Map<String, RedisProperties> tenantMap = ValidateUtils.isEmpty(properties.getTenant()) ? new HashMap<>() : properties.getTenant();
    if(!tenantMap.containsKey(BaseCode.DEFAULT)){
      tenantMap.put(BaseCode.DEFAULT, redisProperties);
      properties.setTenant(tenantMap);
    }
    RedisInitUtils.init(properties.getTenant(), context);
  }

  @Bean(name = "redisTemplate")
  @ConditionalOnMissingBean(RedisTemplate.class)
  public RedisTemplate<String,Object> dynamicRedisTemplate() {
    logger.info("初始化Redis·················");
    RedisTemplate<String, Object> redisTemplate;
    if (isSwitchDb) {
      redisTemplate = new DynamicRedisTemplate<>();
    } else {
      redisTemplate = new RedisTemplate<>();
    }
    // 注入数据源
    redisTemplate.setConnectionFactory(context.getFactoryMap().values().iterator().next());
    // 使用Jackson2JsonRedisSerialize 替换默认序列化
    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
    // key-value结构序列化数据结构
    redisTemplate.setKeySerializer(stringRedisSerializer);
    redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
    // hash数据结构序列化方式,必须这样否则存hash 就是基于jdk序列化的
    redisTemplate.setHashKeySerializer(stringRedisSerializer);
    redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
    // 启用默认序列化方式
    redisTemplate.setEnableDefaultSerializer(true);
    redisTemplate.setEnableTransactionSupport(true);
    redisTemplate.afterPropertiesSet();
    return redisTemplate;
  }

  @Bean(name = "redisFactory")
  @ConditionalOnMissingBean(RedisConnectionFactory.class)
  public RedisConnectionFactory tenantRedisFactory(RedisProperties redisProperties) {
    RedisConnectionFactory factory = null;
    if (isSwitchDb) {
      factory = context.getFactoryMap().values().iterator().next();
    } else {
      factory = RedisInitUtils.initConnectionFactory(redisProperties);
      context.setFactory(BaseCode.DEFAULT, factory);
    }
    return factory;
  }

  @Bean
  @Override
  @ConditionalOnMissingBean(CacheManager.class)
  public CacheManager cacheManager() {
    RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig().computePrefixWith((name -> name + ":"));
    return new SpringRedisAutoManager(DynamicDefaultRedisCacheWriter.nonLockingRedisCacheWriter(context.getFactoryMap().values().iterator().next()), defaultCacheConfig);
  }


  @Bean
  @ConditionalOnMissingBean(ICacheService.class)
  public ICacheService cacheService(RedisTemplate<String,Object> redisTemplate){
    return new RedisService(redisTemplate);
  }
}


