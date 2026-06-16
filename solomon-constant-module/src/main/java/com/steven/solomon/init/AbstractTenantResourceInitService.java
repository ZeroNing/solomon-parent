package com.steven.solomon.init;

import com.steven.solomon.context.TenantContext;
import com.steven.solomon.utils.logger.LoggerUtils;
import cn.hutool.core.util.ObjectUtil;
import java.util.Map;
import org.slf4j.Logger;

/**
 * 多租户资源初始化服务抽象基类。
 *
 * <p>负责遍历租户配置 Map，为每个租户创建对应的资源工厂（如数据源、Redis 连接工厂、MongoClient 等），
 * 并注册到 {@link TenantContext} 中，从而实现多租户场景下的资源隔离。</p>
 *
 * <p>子类只需实现 {@link #initFactory(Object)} 完成具体资源创建；
 * 如需在资源注册成功后执行额外逻辑，可重写 {@link #afterRegistered(String, Object, Object)}。</p>
 *
 * @param <P> 单租户配置属性类型
 * @param <C> 租户上下文类型，必须是 {@link TenantContext} 的子类
 * @param <F> 租户资源工厂类型，例如 DataSource、RedisConnectionFactory
 */
public abstract class AbstractTenantResourceInitService<P, C extends TenantContext<F>, F> {

  /** 日志记录器，统一使用项目封装的 LoggerUtils 获取，便于日志规范化。 */
  protected final Logger log = LoggerUtils.logger(getClass());

  /**
   * 批量初始化多租户资源。
   *
   * <p>遍历所有租户配置，逐个调用 {@link #init(String, Object, TenantContext)} 完成资源创建与注册。
   * 当租户配置为空时跳过初始化并打印警告日志，避免无意义的资源创建。</p>
   *
   * @param propertiesMap 租户编码到租户配置的映射；为空时跳过初始化
   * @param context       租户上下文，用于注册创建好的资源工厂
   * @throws Throwable 资源创建或注册过程中抛出的异常
   */
  public void init(Map<String, P> propertiesMap, C context) throws Throwable {
    // 租户配置为空时无需初始化，直接跳过并记录警告，便于运维定位配置缺失问题
    if (ObjectUtil.isEmpty(propertiesMap)) {
      log.warn("未配置任何租户资源，跳过租户资源初始化");
      return;
    }
    for (Map.Entry<String, P> entry : propertiesMap.entrySet()) {
      init(entry.getKey(), entry.getValue(), context);
    }
  }

  /**
   * 初始化单个租户的资源。
   *
   * <p>先通过 {@link #initFactory(Object)} 创建资源工厂，再注册到租户上下文，
   * 最后回调 {@link #afterRegistered(String, Object, Object)} 供子类扩展。</p>
   *
   * @param tenantCode 租户编码
   * @param properties 该租户的配置属性
   * @param context    租户上下文
   * @throws Throwable 资源创建或注册过程中抛出的异常
   */
  public void init(String tenantCode, P properties, C context) throws Throwable {
    F factory = initFactory(properties);
    context.registerFactory(tenantCode, factory);
    afterRegistered(tenantCode, properties, factory);
  }

  /**
   * 资源注册成功后的扩展点。
   *
   * <p>默认实现为空，子类可重写该方法在资源注册完成后执行额外逻辑，
   * 例如预热连接、注册监控指标等。</p>
   *
   * @param tenantCode 租户编码
   * @param properties 该租户的配置属性
   * @param factory    已注册的资源工厂
   * @throws Throwable 扩展逻辑中抛出的异常
   */
  protected void afterRegistered(String tenantCode, P properties, F factory) throws Throwable {
  }

  /**
   * 创建单个租户的资源工厂。
   *
   * <p>由子类实现具体的资源创建逻辑，例如根据配置构建 HikariDataSource 或 RedisConnectionFactory。</p>
   *
   * @param properties 该租户的配置属性
   * @return 创建好的资源工厂
   * @throws Throwable 资源创建过程中抛出的异常
   */
  public abstract F initFactory(P properties) throws Throwable;
}
