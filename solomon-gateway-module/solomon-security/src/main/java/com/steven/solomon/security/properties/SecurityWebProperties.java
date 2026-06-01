package com.steven.solomon.security.properties;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Web 鉴权配置。
 */
@ConfigurationProperties("security.web")
public class SecurityWebProperties {

  /**
   * 无需登录即可访问的接口。
   */
  private List<String> permitAll = new ArrayList<>(List.of(
      "/auth/**",
      "/actuator/health",
      "/v3/api-docs/**",
      "/swagger-ui/**",
      "/swagger-ui.html"));

  public List<String> getPermitAll() {
    return permitAll;
  }

  public void setPermitAll(List<String> permitAll) {
    this.permitAll = permitAll;
  }
}
