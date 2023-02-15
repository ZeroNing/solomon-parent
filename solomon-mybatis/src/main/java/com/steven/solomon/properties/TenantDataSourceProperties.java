package com.steven.solomon.properties;

import java.util.Map;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "data")
@Component
public class TenantDataSourceProperties {

  /**
   * 租户数据配置
   */
  private Map<String,DataSourceProperties> dataSourceMap;

  public Map<String, DataSourceProperties> getDataSourceMap() {
    return dataSourceMap;
  }

  public void setDataSourceMap(Map<String, DataSourceProperties> dataSourceMap) {
    this.dataSourceMap = dataSourceMap;
  }
}
