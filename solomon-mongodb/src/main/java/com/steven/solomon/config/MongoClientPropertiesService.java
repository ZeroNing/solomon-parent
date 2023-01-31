package com.steven.solomon.config;

import com.steven.solomon.properties.TenantMongoProperties;
import java.util.List;

public interface MongoClientPropertiesService {

  List<TenantMongoProperties> getMongoClient();

  void setCappedCollectionNameMap();
}
