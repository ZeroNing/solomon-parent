package com.steven.solomon.gateway.permission;

import com.steven.solomon.gateway.service.GatewayAnonymousPathProvider;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** 单机模式默认使用的内存权限目录。 */
public class ApiPermissionCatalog implements ApiPermissionStore, GatewayAnonymousPathProvider {

  private final List<ApiPermissionMetadata> permissions = new CopyOnWriteArrayList<>();

  @Override
  public void saveAll(Collection<ApiPermissionMetadata> values) {
    permissions.clear();
    permissions.addAll(values);
  }

  @Override
  public Collection<String> paths() {
    return permissions.stream().filter(ApiPermissionMetadata::anonymous)
        .map(ApiPermissionMetadata::path).toList();
  }

  public List<ApiPermissionMetadata> permissions() {
    return List.copyOf(permissions);
  }
}
