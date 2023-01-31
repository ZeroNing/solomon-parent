package com.steven.solomon.profile;

import com.steven.solomon.enums.CacheModeEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("cache")
@Component
public class CacheProfile {

  /**
   * redis缓存模式（默认单库）
   */
  private String mode = CacheModeEnum.NORMAL.toString();

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }
}
