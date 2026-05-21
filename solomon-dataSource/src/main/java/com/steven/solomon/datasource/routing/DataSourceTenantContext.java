package com.steven.solomon.datasource.routing;

import com.steven.solomon.context.TenantContext;
import com.steven.solomon.datasource.code.DataSourceErrorCode;
import com.steven.solomon.datasource.exception.DataSourceException;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 数据源租户上下文。
 *
 * <p>保存当前线程正在使用的数据源，业务执行完成后必须清理，避免线程池复用导致串租户。</p>
 */
public class DataSourceTenantContext extends TenantContext<DataSource> {

  /** 当前线程绑定的租户编码，便于日志和排查。 */
  private final ThreadLocal<String> currentTenantCode = new ThreadLocal<>();

  /**
   * 按租户编码切换数据源。
   *
   * @param tenantCode 租户编码
   * @throws DataSourceException 数据源未注册时抛出国际化异常
   */
  public void switchTenant(String tenantCode) throws DataSourceException {
    DataSource dataSource = factoryMap.get(tenantCode);
    if (dataSource == null) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_NOT_FOUND, tenantCode);
    }
    threadLocal.set(dataSource);
    currentTenantCode.set(tenantCode);
    logger.info("[DataSource] 切换租户数据源成功: tenant={}", tenantCode);
  }

  @Override
  protected String getCurrentTenantId() {
    String tenantCode = currentTenantCode.get();
    return tenantCode == null ? "unknown" : tenantCode;
  }

  @Override
  public void removeFactory() {
    super.removeFactory();
    currentTenantCode.remove();
  }
}
