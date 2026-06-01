package com.steven.solomon.gatewaysecurity.support;

import com.steven.solomon.gatewaysecurity.model.PermissionDefinition;
import com.steven.solomon.gatewaysecurity.service.PermissionRepository;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** 默认内存权限仓储，适用于开发环境和简单部署。 */
public class InMemoryPermissionRepository implements PermissionRepository {

  private final List<PermissionDefinition> permissions = new CopyOnWriteArrayList<>();

  @Override
  public void saveAll(Collection<PermissionDefinition> permissions) {
    this.permissions.clear();
    this.permissions.addAll(permissions);
  }

  public List<PermissionDefinition> findAll() {
    return List.copyOf(permissions);
  }
}
