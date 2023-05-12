package com.steven.solomon.properties;

import com.steven.solomon.enums.SwitchModeEnum;
import java.util.Map;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.data.mongodb")
@Component
public class TenantMongoProperties {

  public Map<String,MongoProperties> tenant;

  /**
   * mongo缓存模式（默认单库）
   */
  private SwitchModeEnum mode = SwitchModeEnum.NORMAL;

  public SwitchModeEnum getMode() {
    return mode;
  }

  public void setMode(SwitchModeEnum mode) {
    this.mode = mode;
  }

  public Map<String, MongoProperties> getTenant() {
    return tenant;
  }

  public void setTenant(Map<String, MongoProperties> tenant) {
    this.tenant = tenant;
  }

//  public TenantMongoProperties(MongoProperties properties){
//    setAutoIndexCreation(properties.isAutoIndexCreation());
//    setUuidRepresentation(properties.getUuidRepresentation());
//    setFieldNamingStrategy(properties.getFieldNamingStrategy());
//    setReplicaSetName(properties.getReplicaSetName());
//    setPassword(properties.getPassword());
//    setUsername(properties.getUsername());
//    setGridFsDatabase(properties.getGridFsDatabase());
//    setAuthenticationDatabase(properties.getAuthenticationDatabase());
//    setDatabase(properties.getDatabase());
//    setUri(properties.getUri());
//    setPort(properties.getPort());
//    setHost(properties.getHost());
//  }
}
