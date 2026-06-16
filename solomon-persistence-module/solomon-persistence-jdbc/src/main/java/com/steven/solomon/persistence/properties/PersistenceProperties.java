package com.steven.solomon.persistence.properties;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 持久化模块配置�? *
 * <p>单租户与多租户共用一套模型：单租户仅配置一个默认租户，多租户按租户编码配置多个数据源�?/p>
 */
@ConfigurationProperties(prefix = "persistence")
public class PersistenceProperties {

  private boolean enabled = true;

  private String defaultTenant = "default";

  private Map<String, TenantDataSourceProperties> tenants = new LinkedHashMap<>();

  private PaginationProperties page = new PaginationProperties();

  private ScriptProperties script = new ScriptProperties();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getDefaultTenant() {
    return defaultTenant;
  }

  public void setDefaultTenant(String defaultTenant) {
    this.defaultTenant = defaultTenant;
  }

  public Map<String, TenantDataSourceProperties> getTenants() {
    return tenants;
  }

  public void setTenants(Map<String, TenantDataSourceProperties> tenants) {
    this.tenants = tenants;
  }

  public PaginationProperties getPage() {
    return page;
  }

  public void setPage(PaginationProperties page) {
    this.page = page;
  }

  public ScriptProperties getScript() {
    return script;
  }

  public void setScript(ScriptProperties script) {
    this.script = script;
  }
}
