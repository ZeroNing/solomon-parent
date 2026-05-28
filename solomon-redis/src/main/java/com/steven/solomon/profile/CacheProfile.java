package com.steven.solomon.profile;

import com.steven.solomon.pojo.enums.SwitchModeEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Redis缓存模式配置属性。
 *
 * <p>通过 {@code spring.cache.mode} 配置缓存模式，支持：</p>
 * <ul>
 *   <li>NORMAL - 单库模式（默认）</li>
 *   <li>TENANT_PREFIX - 租户前缀模式，在key中拼接租户编码</li>
 *   <li>SWITCH_DB - 多库切换模式，按租户切换不同Redis实例</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "spring.cache")
public class CacheProfile {

  /**
   * redis缓存模式（默认单库）。
   */
  private SwitchModeEnum mode = SwitchModeEnum.NORMAL;

  /**
   * 获取缓存模式。
   * @return 当前缓存模式
   */
  public SwitchModeEnum getMode() {
    return mode;
  }

  /**
   * 设置缓存模式。
   * @param mode 缓存模式
   */
  public void setMode(SwitchModeEnum mode) {
    this.mode = mode;
  }
}
