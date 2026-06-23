package com.steven.solomon.context;

import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * Common configuration model for modules that own tenant-specific resources.
 *
 * @param <T> single-tenant configuration type
 */
public abstract class AbstractTenantProperties<T> {

  private boolean enabled = true;

  private Map<@NotBlank(message = "tenant key must not be blank") String, @Valid T> tenant = new LinkedHashMap<>();

  public boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Map<String, T> getTenant() {
    return tenant;
  }

  public void setTenant(Map<String, T> tenant) {
    this.tenant = tenant;
  }
}
