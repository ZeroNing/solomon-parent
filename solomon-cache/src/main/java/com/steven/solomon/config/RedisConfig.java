package com.steven.solomon.config;

import com.steven.solomon.enums.CacheTypeEnum;
import com.steven.solomon.enums.SwitchModeEnum;
import com.steven.solomon.init.RedisInitUtils;
import com.steven.solomon.logger.LoggerUtils;
import com.steven.solomon.manager.DynamicDefaultRedisCacheWriter;
import com.steven.solomon.manager.SpringRedisAutoManager;
import com.steven.solomon.profile.TenantRedisProperties;
import com.steven.solomon.serializer.BaseRedisSerializer;
import com.steven.solomon.service.impl.RedisService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.template.DynamicRedisTemplate;
import com.steven.solomon.verification.ValidateUtils;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration(proxyBeanMethods = false)
@Order(2)
@EnableConfigurationProperties(value={RedisProperties.class,TenantRedisProperties.class})
@Import(value = {RedisTenantContext.class,RedisService.class})
public class RedisConfig extends CachingConfigurerSupport {

  private Logger logger = LoggerUtils.logger(getClass());

  private final TenantRedisProperties properties;

  @Value("${cache.type}")
  private String cacheType;

  private final RedisTenantContext context;

  private boolean isSwitchDb = false;

  private final RedisProperties redisProperties;

  private final ApplicationContext applicationContext;

  public RedisConfig(TenantRedisProperties properties, RedisTenantContext context, RedisProperties redisProperties,
      ApplicationContext applicationContext) {
    this.properties      = properties;
    this.context         = context;
    this.isSwitchDb      = ValidateUtils.equalsIgnoreCase(SwitchModeEnum.SWITCH_DB.toString(), properties.getMode().toString());
    this.redisProperties = redisProperties;
    this.applicationContext = applicationContext;
  }

  @PostConstruct
  public void afterPropertiesSet() {
    SpringUtil.setContext(applicationContext);
    if (!ValidateUtils.equalsIgnoreCase(CacheTypeEnum.REDIS.toString(), cacheType)) {
      return;
    }
    logger.info("Cache当前类型为:{}",properties.getType().getDesc());
    logger.info("Redis当前模式为:{}",properties.getMode().getDesc());
    Map<String, RedisProperties> tenantMap = ValidateUtils.isEmpty(properties.getTenant()) ? new HashMap<>() : properties.getTenant();
    if(!tenantMap.containsKey("default")){
      tenantMap.put("default", redisProperties);
      properties.setTenant(tenantMap);
    }
    RedisInitUtils.init(properties.getTenant(), context);
  }

  @Bean(name = "redisTemplate")
  public RedisTemplate dynamicRedisTemplate() {
    logger.info("初始化redis start");
    RedisTemplate<String, Object> redisTemplate;
    if (isSwitchDb) {
      redisTemplate = new DynamicRedisTemplate<String, Object>();
    } else {
      redisTemplate = new RedisTemplate<String, Object>();
    }
    // 注入数据源
    redisTemplate.setConnectionFactory(context.getFactoryMap().values().iterator().next());
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
  public RedisConnectionFactory tenantRedisFactory(RedisProperties redisProperties) {
    RedisConnectionFactory factory = null;
    if (isSwitchDb) {
      factory = context.getFactoryMap().values().iterator().next();
    } else {
      factory = RedisInitUtils.initConnectionFactory(redisProperties);
      context.setFactory("default", factory);
    }
    return factory;
  }

  @Bean
  @Override
  public CacheManager cacheManager() {
    RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
        .computePrefixWith((name -> name + ":"));
    SpringRedisAutoManager springRedisAutoManager = new SpringRedisAutoManager(
        DynamicDefaultRedisCacheWriter.nonLockingRedisCacheWriter(
            context.getFactoryMap().values().iterator().next()), defaultCacheConfig);
    return springRedisAutoManager;
  }


}


