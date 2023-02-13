package com.steven.solomon.config;

import com.steven.solomon.properties.TenantMongoProperties;
import java.util.List;

public interface MongoClientPropertiesService {

  List<TenantMongoProperties> getMongoClient();

  /**
   * 设置Map Key:集合名,Value:集合对应的Class
   */
  void setCappedCollectionNameMap();
}
