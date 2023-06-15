package com.steven.solomon.profile;

import com.steven.solomon.pojo.enums.SwitchModeEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.cache")
public class CacheProfile {

  /**
   * redis缓存模式（默认单库）
   */
  private SwitchModeEnum mode = SwitchModeEnum.NORMAL;

  public SwitchModeEnum getMode() {
    return mode;
  }

  public void setMode(SwitchModeEnum mode) {
    this.mode = mode;
  }
}
