package com.steven.solomon.manager;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.pojo.enums.SwitchModeEnum;
import com.steven.solomon.profile.CacheProfile;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.slf4j.Logger;
import org.springframework.cache.Cache;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.Duration;

public class SpringRedisAutoManager extends RedisCacheManager {

  private Logger log = LoggerUtils.logger(SpringRedisAutoManager.class);

  @Resource
  private CacheProfile cacheProfile;

  /**
   * 缓存参数的分隔符
   * 数组元素0=缓存的名称
   * 数组元素1=缓存过期时间TTL
   * 数组元素2=缓存在多少秒开始主动失效来强制刷新
   */
  public static final String separator = "@@";

  public SpringRedisAutoManager(RedisCacheWriter cacheWriter,
      RedisCacheConfiguration defaultCacheConfiguration) {
    super(cacheWriter, defaultCacheConfiguration);
  }

  /**
   * 从上下文中获取租户ID，重写@Cacheable value 值
   * @param name
   * @return
   */
  @Override
  public Cache getCache(String name) {
    String tenantCode = RequestHeaderHolder.getTenantCode();
    if(SwitchModeEnum.TENANT_PREFIX.toString().equals(cacheProfile.getMode()) && ValidateUtils.isNotEmpty(tenantCode)) {
      name = tenantCode + StrUtil.COLON + name;
    } else if(ValidateUtils.isEmpty(tenantCode)){
      log.info("在{}模式下,获取到的租户id为空,将redis的Key转为默认模式",cacheProfile.getMode());
    }
    return super.getCache(name);
  }

  @Override
  protected RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
    String[] array = StringUtils.delimitedListToStringArray(name, separator);
    name = array[0];
    // 解析TTL
    if (array.length > 1) {
      long ttl = Long.parseLong(array[1]);
      // 注意单位我此处用的是秒，而非毫秒
      cacheConfig = cacheConfig.entryTtl(Duration.ofSeconds(ttl));
    }
    return super.createRedisCache(name, cacheConfig);
  }
}
