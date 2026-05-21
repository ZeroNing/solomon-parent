package com.steven.solomon.datasource.aspect;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.datasource.routing.DataSourceTenantContext;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;

/**
 * 数据源租户切换AOP。
 *
 * <p>在常见数据库访问入口前，根据当前请求上下文中的租户编码切换数据源，执行完成后自动清理。</p>
 */
@Aspect
public class DataSourceTenantAspect {

  private final Logger logger = LoggerUtils.logger(getClass());

  private final DataSourceTenantContext context;

  public DataSourceTenantAspect(DataSourceTenantContext context) {
    this.context = context;
  }

  @Pointcut("execution(* org.springframework.jdbc.core.JdbcTemplate.*(..)) || "
      + "execution(* org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate.*(..)) || "
      + "execution(* org.springframework.data.repository.Repository+.*(..)) || "
      + "execution(* com.baomidou.mybatisplus.core.mapper.BaseMapper+.*(..))")
  void dataSourcePointCut() {
  }

  @Around("dataSourcePointCut()")
  public Object around(ProceedingJoinPoint point) throws Throwable {
    String tenantCode = ValidateUtils.isNotEmpty(RequestHeaderHolder.getTenantCode()) ? RequestHeaderHolder.getTenantCode() : BaseCode.DEFAULT;
    logger.info("[DataSource] AOP切换租户数据源: tenant={}", tenantCode);
    context.switchTenant(tenantCode);
    try {
      return point.proceed();
    } finally {
      context.removeFactory();
    }
  }
}
