package com.steven.solomon.persistence.aspect;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.persistence.properties.PersistenceProperties;
import com.steven.solomon.persistence.routing.DataSourceTenantContext;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;

/**
 * 数据源租户切换AOP。
 *
 * <p>在常见数据库访问入口和Repository仓储入口前，根据请求头中的租户编码切换数据源；
 * 执行完成后自动清理线程上下文。</p>
 */
@Aspect
public class DataSourceTenantAspect {

  private final Logger logger = LoggerUtils.logger(getClass());

  private final DataSourceTenantContext context;

  private final PersistenceProperties properties;

  /**
   * 构造数据源租户切面。
   *
   * @param context 数据源租户上下文
   * @param properties 动态数据源配置，用于读取默认租户
   */
  public DataSourceTenantAspect(
      DataSourceTenantContext context,
      PersistenceProperties properties) {
    this.context = context;
    this.properties = properties;
  }

  @Pointcut("execution(* com.steven.solomon.persistence.sql.SqlExecutor.*(..)) || "
      + "execution(* com.steven.solomon.persistence.sql.Repository+.*(..))")
  void dataSourcePointCut() {
  }

  /**
   * 在数据库访问入口周围切换租户数据源。
   *
   * @param point AOP连接点
   * @return 原方法执行结果
   * @throws Throwable 原方法执行异常或数据源切换异常
   */
  @Around("dataSourcePointCut()")
  public Object around(ProceedingJoinPoint point) throws Throwable {
    String tenantCode = resolveTenantCode();
    logger.info("[DataSource] AOP切换租户数据源 tenant={}", tenantCode);
    context.switchTenant(tenantCode);
    try {
      return point.proceed();
    } finally {
      context.removeFactory();
    }
  }

  private String resolveTenantCode() {
    String tenantCode = "";
    try {
      tenantCode = RequestHeaderHolder.getTenantCode();
    } catch (Exception e) {
      logger.debug("[DataSource] 读取请求头租户编码失败，使用默认租户", e);
    }
    if (StrUtil.isNotBlank(tenantCode)) {
      return tenantCode.trim();
    }
    return properties.getDefaultTenant();
  }
}
