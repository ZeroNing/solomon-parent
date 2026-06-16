package com.steven.solomon.properties;

import com.steven.solomon.context.AbstractTenantProperties;
import com.steven.solomon.pojo.enums.SwitchModeEnum;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tenant-specific MongoDB connection properties under {@code spring.data.mongodb.tenant}.
 */
@ConfigurationProperties(prefix = "spring.data.mongodb")
public class TenantMongoProperties extends AbstractTenantProperties<MongoProperties> {

  private SwitchModeEnum mode = SwitchModeEnum.NORMAL;

  public SwitchModeEnum getMode() {
    return mode;
  }

  public void setMode(SwitchModeEnum mode) {
    this.mode = mode;
  }
}
