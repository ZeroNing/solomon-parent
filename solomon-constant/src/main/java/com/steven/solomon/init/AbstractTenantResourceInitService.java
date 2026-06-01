package com.steven.solomon.init;

import com.steven.solomon.context.TenantContext;
import com.steven.solomon.utils.logger.LoggerUtils;
import cn.hutool.core.util.ObjectUtil;
import java.util.Map;
import org.slf4j.Logger;

/**
 * Initializes and registers tenant-specific resources.
 *
 * @param <P> single-tenant configuration type
 * @param <C> tenant context type
 * @param <F> tenant resource type
 */
public abstract class AbstractTenantResourceInitService<P, C extends TenantContext<F>, F> {

  protected final Logger log = LoggerUtils.logger(getClass());

  public void init(Map<String, P> propertiesMap, C context) throws Throwable {
    if (ObjectUtil.isEmpty(propertiesMap)) {
      log.warn("No tenant resources configured, skipping initialization");
      return;
    }
    for (Map.Entry<String, P> entry : propertiesMap.entrySet()) {
      init(entry.getKey(), entry.getValue(), context);
    }
  }

  public void init(String tenantCode, P properties, C context) throws Throwable {
    F factory = initFactory(properties);
    context.registerFactory(tenantCode, factory);
    afterRegistered(tenantCode, properties, factory);
  }

  /**
   * Extension point for module-specific initialization after a resource is registered.
   */
  protected void afterRegistered(String tenantCode, P properties, F factory) throws Throwable {
  }

  public abstract F initFactory(P properties) throws Throwable;
}
