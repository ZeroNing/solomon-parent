package com.steven.solomon.template;

import cn.hutool.core.util.ObjectUtil;

import com.steven.solomon.config.RedisTenantContext;
import com.steven.solomon.spring.SpringUtil;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 动态Redis模板，支持多租户数据源切换。
 *
 * <p>重写 {@link #getConnectionFactory()} 方法，每次操作时从租户上下文中
 * 获取当前线程对应的Redis连接工厂，实现线程级别的数据源动态切换。</p>
 *
 * @param <K> key类型
 * @param <V> value类型
 */
public class DynamicRedisTemplate<K,V> extends RedisTemplate<K,V> {

  public DynamicRedisTemplate() {
    super();
  }

  /**
   * 获取当前线程绑定的Redis连接工厂。
   *
   * <p>优先从 {@link RedisTenantContext} 中获取当前租户的连接工厂，
   * 如果当前线程未绑定租户，则回退到父类的默认连接工厂。</p>
   *
   * @return 当前租户的Redis连接工厂，或默认连接工厂
   */
  @Override
  public RedisConnectionFactory getConnectionFactory() {
    // 从租户上下文获取当前线程的连接工厂
    RedisConnectionFactory factory = SpringUtil.getBean(RedisTenantContext.class).getFactory();
    return ObjectUtil.defaultIfNull(factory,super.getConnectionFactory());
  }
}
