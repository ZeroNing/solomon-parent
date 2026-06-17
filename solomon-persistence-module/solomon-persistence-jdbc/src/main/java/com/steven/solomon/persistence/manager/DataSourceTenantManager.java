package com.steven.solomon.persistence.manager;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.persistence.code.PersistenceErrorCode;
import com.steven.solomon.persistence.exception.PersistenceException;
import com.steven.solomon.persistence.factory.DynamicDataSourceFactory;
import com.steven.solomon.persistence.properties.PersistenceProperties;
import com.steven.solomon.persistence.properties.TenantDataSourceProperties;
import com.steven.solomon.persistence.routing.DataSourceTenantContext;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据源租户运行时管理器。
 *
 * <p>用于在服务不重启的情况下新增、更新或移除租户数据源。新增租户后会同步更新配置缓存和
 * {@link DataSourceTenantContext}，后续通过租户编码切换数据源时即可直接命中新注册的数据源。</p>
 */
public class DataSourceTenantManager {

  private static final Logger log = LoggerFactory.getLogger(DataSourceTenantManager.class);

  private final PersistenceProperties properties;

  private final DynamicDataSourceFactory factory;

  private final DataSourceTenantContext context;

  /**
   * 构造数据源租户运行时管理器。
   *
   * @param properties 数据源配置对象，用于维护运行时租户配置缓存
   * @param factory 动态数据源工厂，用于根据租户配置创建 Hikari 或 Druid 数据源
   * @param context 数据源租户上下文，用于注册和注销可切换的数据源实例
   */
  public DataSourceTenantManager(
      PersistenceProperties properties,
      DynamicDataSourceFactory factory,
      DataSourceTenantContext context) {
    this.properties = properties;
    this.factory = factory;
    this.context = context;
  }

  /**
   * 新增或更新租户数据源。
   *
   * <p>如果租户已经存在，会先创建新的数据源并注册成功，再关闭旧数据源，避免更新失败时影响原有租户。
   * 该方法只更新当前服务进程内的运行时配置；如果使用 Nacos、Apollo 或数据库保存租户配置，
   * 调用方还需要先把配置持久化，再调用该方法刷新运行时数据源。</p>
   *
   * @param tenantCode 租户编码，不能为空，后续切换数据源时使用该编码
   * @param tenantProperties 租户数据源配置，包含数据库类型、连接池类型、JDBC 地址、账号密码等
   * @return 新创建并注册成功的数据源实例
   * @throws PersistenceException 租户编码为空、配置为空或数据源创建失败时抛出
   */
  public synchronized DataSource addOrUpdateTenant(
      String tenantCode,
      TenantDataSourceProperties tenantProperties) throws PersistenceException {
    String normalizedTenantCode = requireTenantCode(tenantCode);
    if (ObjectUtil.isEmpty(tenantProperties)) {
      throw new PersistenceException(PersistenceErrorCode.DATA_SOURCE_CONFIG_NOT_FOUND);
    }

    DataSource newDataSource = factory.createDataSource(tenantProperties);
    DataSource oldDataSource = context.getFactoryMap().get(normalizedTenantCode);
    properties.getTenants().put(normalizedTenantCode, tenantProperties);
    context.registerFactory(normalizedTenantCode, newDataSource);
    closeQuietly(oldDataSource, normalizedTenantCode);
    log.info("[DataSource] 运行时注册租户数据源成功 tenant={}", normalizedTenantCode);
    return newDataSource;
  }

  /**
   * 新增租户数据源。
   *
   * <p>租户已经存在时会抛出异常，适合只允许创建一次的租户开通流程。</p>
   *
   * @param tenantCode 租户编码，不能为空
   * @param tenantProperties 租户数据源配置，不能为空
   * @return 新创建并注册成功的数据源实例
   * @throws PersistenceException 租户已存在、配置为空或数据源创建失败时抛出
   */
  public synchronized DataSource addTenant(
      String tenantCode,
      TenantDataSourceProperties tenantProperties) throws PersistenceException {
    String normalizedTenantCode = requireTenantCode(tenantCode);
    if (context.isRegistered(normalizedTenantCode)) {
      throw new PersistenceException(PersistenceErrorCode.DATA_SOURCE_TENANT_ALREADY_EXISTS,
          normalizedTenantCode);
    }
    return addOrUpdateTenant(normalizedTenantCode, tenantProperties);
  }

  /**
   * 移除租户数据源。
   *
   * <p>该方法会从运行时配置缓存和租户上下文中移除租户，并尝试关闭旧连接池。
   * 如果该租户正在被其他线程使用，已获取的连接不会被强制中断，但后续切换会失败。</p>
   *
   * @param tenantCode 租户编码，不能为空
   * @return 被移除的数据源实例；租户不存在时返回 {@code null}
   * @throws PersistenceException 租户编码为空时抛出
   */
  public synchronized DataSource removeTenant(String tenantCode) throws PersistenceException {
    String normalizedTenantCode = requireTenantCode(tenantCode);
    properties.getTenants().remove(normalizedTenantCode);
    DataSource removedDataSource = context.unregisterFactory(normalizedTenantCode);
    closeQuietly(removedDataSource, normalizedTenantCode);
    log.info("[DataSource] 运行时移除租户数据源 tenant={}", normalizedTenantCode);
    return removedDataSource;
  }

  /**
   * 判断租户数据源是否已经注册。
   *
   * @param tenantCode 租户编码
   * @return 已注册返回 {@code true}，否则返回 {@code false}
   */
  public boolean exists(String tenantCode) {
    return StrUtil.isNotBlank(tenantCode) && context.isRegistered(tenantCode.trim());
  }

  /**
   * 获取当前服务进程内已经注册的租户编码集合。
   *
   * @return 当前已注册租户编码集合，只读快照
   */
  public Set<String> tenantCodes() {
    return context.getFactoryMap().keySet();
  }

  /**
   * 获取当前服务进程内的租户数据源配置快照。
   *
   * @return 租户配置快照，只读 Map
   */
  public Map<String, TenantDataSourceProperties> tenantProperties() {
    return Map.copyOf(properties.getTenants());
  }

  private String requireTenantCode(String tenantCode) throws PersistenceException {
    if (StrUtil.isBlank(tenantCode)) {
      throw new PersistenceException(PersistenceErrorCode.DATA_SOURCE_NOT_FOUND, tenantCode);
    }
    return tenantCode.trim();
  }

  private void closeQuietly(DataSource dataSource, String tenantCode) {
    if (ObjectUtil.isEmpty(dataSource)) {
      return;
    }
    if (dataSource instanceof AutoCloseable closeable) {
      try {
        closeable.close();
      } catch (Exception e) {
        log.warn("[DataSource] 关闭旧租户数据源失败 tenant={}, error={}", tenantCode,
            e.getMessage(), e);
      }
    }
  }
}
