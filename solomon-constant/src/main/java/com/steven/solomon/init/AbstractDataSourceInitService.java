package com.steven.solomon.init;

import com.steven.solomon.context.TenantContext;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;

import java.util.Map;

/**
 * 数据源初始化服务基类，负责多租户场景下组件的批量与单租户初始化。
 *
 * <p>子类需要实现 {@link #init(String, P, C)} 和 {@link #initFactory(P)} 方法，
 * 完成具体的数据源、Redis或MongoDB工厂的创建逻辑。</p>
 *
 * @param <P> 单租户配置属性类型
 * @param <C> 租户上下文类型，继承 {@link TenantContext}
 * @param <F> 工厂对象类型，例如 DataSource、RedisConnectionFactory
 */
public abstract class AbstractDataSourceInitService<P, C extends TenantContext<?>, F> {

  protected final Logger log = LoggerUtils.logger(getClass());

  /**
   * 批量初始化所有租户的组件。
   *
   * <p>遍历配置映射，逐个调用 {@link #init(String, P, C)} 完成初始化。</p>
   *
   * @param propertiesMap 租户编码与配置属性的映射
   * @param context       租户上下文对象
   * @throws Throwable 初始化过程中可能抛出的异常
   */
  public void init(Map<String, P> propertiesMap, C context) throws Throwable {
    for (Map.Entry<String, P> entry : propertiesMap.entrySet()) {
      init(entry.getKey(), entry.getValue(), context);
    }
  }

  /**
   * 初始化单个租户组件
   *
   * @param tenantCode 租户编码
   * @param properties 单个租户组件配置
   * @param context    租户切换类
   */
  public abstract void init(String tenantCode, P properties, C context) throws Throwable;

  /**
   * 初始化工厂/数据库 dataSource
   *
   * @param properties 单个租户组件配置
   * @return 工厂类/数据库 dataSource
   */
  public abstract F initFactory(P properties) throws Throwable;
}
