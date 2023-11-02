package com.steven.solomon.config;

import com.steven.solomon.condition.RedisCondition;
import com.steven.solomon.init.RedisInitUtils;
import com.steven.solomon.manager.DynamicDefaultRedisCacheWriter;
import com.steven.solomon.manager.SpringRedisAutoManager;
import com.steven.solomon.pojo.enums.SwitchModeEnum;
import com.steven.solomon.profile.CacheProfile;
import com.steven.solomon.profile.TenantRedisProperties;
import com.steven.solomon.serializer.BaseRedisSerializer;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.template.DynamicRedisTemplate;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableConfigurationProperties(value={RedisProperties.class,TenantRedisProperties.class, CacheProfile.class,CacheProperties.class})
@Import(value = {RedisTenantContext.class})
public class RedisConfig extends CachingConfigurerSupport {

  private Logger logger = LoggerUtils.logger(getClass());

  private final TenantRedisProperties properties;

  private final RedisTenantContext context;

  private boolean isSwitchDb = false;

  private final RedisProperties redisProperties;

  private final ApplicationContext applicationContext;

  private final CacheProfile cacheProfile;

  private final CacheProperties cacheProperties;

  public RedisConfig(TenantRedisProperties properties, RedisTenantContext context, RedisProperties redisProperties,
      ApplicationContext applicationContext, CacheProfile cacheProfile,
      CacheProperties cacheProperties) {
    this.properties         = properties;
    this.context            = context;
    this.isSwitchDb         = ValidateUtils.equalsIgnoreCase(SwitchModeEnum.SWITCH_DB.toString(), cacheProfile.getMode().toString());
    this.redisProperties    = redisProperties;
    this.applicationContext = applicationContext;
    this.cacheProfile       = cacheProfile;
    this.cacheProperties    = cacheProperties;
  }

  @PostConstruct
  public void afterPropertiesSet() {
    SpringUtil.setContext(applicationContext);
    if (!ValidateUtils.equalsIgnoreCase(CacheType.REDIS.toString(), cacheProperties.getType().name())) {
      return;
    }
    logger.info("Cache当前类型为:{}", cacheProperties.getType().name());
    logger.info("Redis当前模式为:{}", cacheProfile.getMode().getDesc());
    Map<String, RedisProperties> tenantMap = ValidateUtils.isEmpty(properties.getTenant()) ? new HashMap<>() : properties.getTenant();
    if(!tenantMap.containsKey("default")){
      tenantMap.put("default", redisProperties);
      properties.setTenant(tenantMap);
    }
    RedisInitUtils.init(properties.getTenant(), context);
  }

  @Bean(name = "redisTemplate")
  @Conditional(value= RedisCondition.class)
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
  @Conditional(value= RedisCondition.class)
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
  @ConditionalOnMissingBean(CacheManager.class)
  public CacheManager cacheManager() {
    switch (CacheType.REDIS){
      case REDIS:
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig().computePrefixWith((name -> name + ":"));
        return new SpringRedisAutoManager(DynamicDefaultRedisCacheWriter.nonLockingRedisCacheWriter(context.getFactoryMap().values().iterator().next()), defaultCacheConfig);
      default :
        return new NoOpCacheManager();
    }
  }
}


