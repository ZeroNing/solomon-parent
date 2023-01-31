package com.steven.solomon.properties;

import org.springframework.boot.autoconfigure.mongo.MongoProperties;

public class TenantMongoProperties extends MongoProperties {

  private String tenantCode;

  public String getTenantCode() {
    return tenantCode;
  }

  public void setTenantCode(String tenantCode) {
    this.tenantCode = tenantCode;
  }

  public TenantMongoProperties(MongoProperties properties){
    setAutoIndexCreation(properties.isAutoIndexCreation());
    setUuidRepresentation(properties.getUuidRepresentation());
    setFieldNamingStrategy(properties.getFieldNamingStrategy());
    setReplicaSetName(properties.getReplicaSetName());
    setPassword(properties.getPassword());
    setUsername(properties.getUsername());
    setGridFsDatabase(properties.getGridFsDatabase());
    setAuthenticationDatabase(properties.getAuthenticationDatabase());
    setDatabase(properties.getDatabase());
    setUri(properties.getUri());
    setPort(properties.getPort());
    setHost(properties.getHost());
  }
}
