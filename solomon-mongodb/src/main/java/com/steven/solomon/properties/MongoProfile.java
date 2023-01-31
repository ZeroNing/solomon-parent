package com.steven.solomon.properties;

import com.steven.solomon.enums.MongoModeEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("mongo")
@Component
public class MongoProfile {

  /**
   * mongo缓存模式（默认单库）
   */
  private String mode = MongoModeEnum.NORMAL.toString();

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }
}
