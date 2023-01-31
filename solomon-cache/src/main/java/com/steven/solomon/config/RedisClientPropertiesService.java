package com.steven.solomon.config;

import com.steven.solomon.profile.TenantRedisProperties;
import java.util.List;

public interface RedisClientPropertiesService {

  List<TenantRedisProperties> getRedisClient();
}
