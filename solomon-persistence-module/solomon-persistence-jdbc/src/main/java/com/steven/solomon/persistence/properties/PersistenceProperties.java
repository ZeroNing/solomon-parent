package com.steven.solomon.persistence.properties;

import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 持久化模块配置。
 *
 * <p>单租户与多租户共用一套模型：单租户仅配置一个默认租户，多租户按租户编码配置多个数据源。</p>
 */
@ConfigurationProperties(prefix = "persistence")
@Validated
public class PersistenceProperties {

  private boolean enabled = true;

  @NotBlank(message = "persistence.default-tenant must not be blank")
  private String defaultTenant = "default";

  private Map<@NotBlank(message = "persistence.tenants key must not be blank") String, @Valid TenantDataSourceProperties> tenants = new LinkedHashMap<>();

  @Valid
  @NotNull(message = "persistence.page must not be null")
  private PaginationProperties page = new PaginationProperties();

  @Valid
  @NotNull(message = "persistence.script must not be null")
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
