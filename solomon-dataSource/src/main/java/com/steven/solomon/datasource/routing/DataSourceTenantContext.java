package com.steven.solomon.datasource.routing;

import com.steven.solomon.context.TenantContext;
import com.steven.solomon.datasource.code.DataSourceErrorCode;
import com.steven.solomon.datasource.exception.DataSourceException;
import javax.sql.DataSource;

/**
 * 数据源租户上下文。
 *
 * <p>保存当前线程正在使用的租户数据源，业务执行完成后必须清理，避免线程池复用导致串租户。</p>
 */
public class DataSourceTenantContext extends TenantContext<DataSource> {

  /** 当前线程绑定的租户编码，便于日志输出和SQL方言判断。 */
  private final ThreadLocal<String> currentTenantCode = new ThreadLocal<>();

  /** 当前线程切换深度，用于支持AOP嵌套调用。 */
  private final ThreadLocal<Integer> switchDepth = ThreadLocal.withInitial(() -> 0);

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
    switchDepth.set(switchDepth.get() + 1);
    logger.info("[DataSource] 切换租户数据源成功 tenant={}", tenantCode);
  }

  /**
   * 获取当前线程绑定的租户编码。
   *
   * @return 当前租户编码；未切换时返回null
   */
  public String getCurrentTenantCode() {
    return currentTenantCode.get();
  }

  /**
   * 获取当前租户标识，用于父类日志输出。
   *
   * @return 当前租户编码；未切换时返回unknown
   */
  @Override
  protected String getCurrentTenantId() {
    String tenantCode = currentTenantCode.get();
    return tenantCode == null ? "unknown" : tenantCode;
  }

  /**
   * 清理当前线程绑定的数据源。
   *
   * <p>AOP嵌套调用时只减少切换深度，最外层调用结束后才真正清理。</p>
   */
  @Override
  public void removeFactory() {
    int depth = switchDepth.get() - 1;
    if (depth > 0) {
      switchDepth.set(depth);
      return;
    }
    switchDepth.remove();
    super.removeFactory();
    currentTenantCode.remove();
  }
}
