package com.steven.solomon.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.steven.solomon.code.BaseCode;
import com.steven.solomon.init.AbstractDataSourceInitService;
import com.steven.solomon.init.DefaultRedisInitService;
import com.steven.solomon.json.config.JsonConfig;
import com.steven.solomon.manager.DynamicDefaultRedisCacheWriter;
import com.steven.solomon.manager.SpringRedisAutoManager;
import com.steven.solomon.pojo.enums.SwitchModeEnum;
import com.steven.solomon.profile.CacheProfile;
import com.steven.solomon.profile.TenantRedisProperties;
import com.steven.solomon.service.ICacheService;
import com.steven.solomon.service.RedisService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.template.DynamicRedisTemplate;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import jakarta.annotation.PostConstruct;
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
import org.springframework.core.ResolvableType;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis核心配置类。
 *
 * <p>负责Redis多租户连接工厂初始化、RedisTemplate配置、CacheManager配置等核心功能。</p>
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>初始化各租户的Redis连接工厂</li>
 *   <li>配置RedisTemplate（支持动态数据源切换）</li>
 *   <li>配置Spring CacheManager</li>
 *   <li>配置ICacheService实例</li>
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(value={RedisProperties.class,TenantRedisProperties.class, CacheProfile.class,CacheProperties.class})
@Import(value = {RedisTenantContext.class, JsonConfig.class})
public class RedisConfig extends CachingConfigurerSupport {

  /** 日志记录器 */
  private final Logger logger = LoggerUtils.logger(getClass());

  /** 多租户Redis配置 */
  private final TenantRedisProperties properties;

  /** Redis租户上下文 */
  private final RedisTenantContext context;

  /** 是否为多库切换模式 */
  private final boolean isSwitchDb;

  /** Redis连接配置 */
  private final RedisProperties redisProperties;

  /** 缓存模式配置 */
  private final CacheProfile cacheProfile;

  /** JSON序列化器 */
  private final ObjectMapper objectMapper;


  /**
   * 构造函数，注入所需依赖。
   *
   * @param tenantRedisProperties 多租户Redis配置
   * @param redisTenantContext    Redis租户上下文
   * @param redisPropertiesConfig Redis连接配置
   * @param applicationContext    Spring应用上下文
   * @param cacheProfileConfig    缓存模式配置
   * @param objectMapperConfig    JSON序列化器
   */
  public RedisConfig(TenantRedisProperties tenantRedisProperties, RedisTenantContext redisTenantContext,
                     RedisProperties redisPropertiesConfig, ApplicationContext applicationContext,
                     CacheProfile cacheProfileConfig, ObjectMapper objectMapperConfig) {
    this.properties = tenantRedisProperties;
    this.context = redisTenantContext;
    this.isSwitchDb = ValidateUtils.equalsIgnoreCase(
        SwitchModeEnum.SWITCH_DB.toString(), cacheProfileConfig.getMode().toString());
    this.redisProperties = redisPropertiesConfig;
    this.cacheProfile = cacheProfileConfig;
    this.objectMapper = objectMapperConfig;
    SpringUtil.setContext(applicationContext);
  }

  /**
   * Bean初始化后回调，完成Redis连接工厂的初始化。
   *
   * <p>如果租户配置中不包含默认租户，自动将当前Redis配置作为默认租户添加。</p>
   *
   * @throws Throwable 初始化失败时抛出
   */
  @PostConstruct
  public void afterPropertiesSet() throws Throwable {
    logger.info("Redis当前模式为:{}", cacheProfile.getMode().getDesc());
    // 构建租户配置映射表，如果没有配置则使用默认Redis配置
    Map<String, RedisProperties> tenantMap = ValidateUtils.isEmpty(properties.getTenant()) 
        ? new HashMap<>() 
        : properties.getTenant();
    // 确保存在默认租户配置
    if (!tenantMap.containsKey(BaseCode.DEFAULT)) {
      tenantMap.put(BaseCode.DEFAULT, redisProperties);
      properties.setTenant(tenantMap);
    }
    // 获取初始化服务并执行初始化
    AbstractDataSourceInitService<RedisProperties, RedisTenantContext, LettuceConnectionFactory> service = getService();
    service.init(properties.getTenant(), context);
  }

  /**
   * 创建RedisTemplate实例。
   *
   * <p>多库切换模式下使用 {@link DynamicRedisTemplate}，普通模式使用标准 {@link RedisTemplate}。</p>
   * <p>序列化配置：key使用String序列化，value使用Jackson JSON序列化。</p>
   *
   * @return RedisTemplate实例
   */
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

  /**
   * 创建Redis连接工厂。
   *
   * <p>多库切换模式下直接使用租户上下文中的默认工厂；
   * 普通模式下创建新的连接工厂并注册到租户上下文。</p>
   *
   * @param redisProperties Redis连接配置
   * @return Redis连接工厂
   * @throws Throwable 创建失败时抛出
   */
  @Bean(name = "redisFactory")
  @ConditionalOnMissingBean(RedisConnectionFactory.class)
  public RedisConnectionFactory tenantRedisFactory(RedisProperties redisProperties) throws Throwable {
    RedisConnectionFactory factory;
    if (isSwitchDb) {
      factory = context.getFactoryMap().values().iterator().next();
    } else {
      factory = new DefaultRedisInitService().initFactory(redisProperties);
      context.registerFactory(BaseCode.DEFAULT, factory);
    }
    return factory;
  }

  /**
   * 创建Spring CacheManager。
   *
   * <p>使用 {@link SpringRedisAutoManager} 支持动态租户缓存管理。</p>
   *
   * @return CacheManager实例
   */
  @Bean
  @Override
  @ConditionalOnMissingBean(CacheManager.class)
  public CacheManager cacheManager() {
    RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig().computePrefixWith((name -> name + ":"));
    return new SpringRedisAutoManager(DynamicDefaultRedisCacheWriter.nonLockingRedisCacheWriter(context.getFactoryMap().values().iterator().next()), defaultCacheConfig);
  }


  /**
   * 创建缓存服务实例。
   *
   * @param redisTemplate Redis模板
   * @return ICacheService实例
   */
  @Bean
  @ConditionalOnMissingBean(ICacheService.class)
  public ICacheService cacheService(RedisTemplate<String,Object> redisTemplate) {
    return new RedisService(redisTemplate);
  }

  /**
   * 获取Redis数据源初始化服务实例。
   *
   * <p>优先从Spring容器中查找匹配类型的Bean，找不到则使用默认的 {@link DefaultRedisInitService}。</p>
   *
   * @return 数据源初始化服务
   */
  private AbstractDataSourceInitService<RedisProperties,RedisTenantContext, LettuceConnectionFactory> getService() {
    return SpringUtil.getBeansOfType(ResolvableType.forClassWithGenerics(
            AbstractDataSourceInitService.class,
            ResolvableType.forClass(RedisProperties.class),  // 替换P为实际类型
            ResolvableType.forClass(RedisTenantContext.class),  // 替换C为实际类型
            ResolvableType.forClass(LettuceConnectionFactory.class)   // 替换F为实际类型
    ),new DefaultRedisInitService());
  }
}


