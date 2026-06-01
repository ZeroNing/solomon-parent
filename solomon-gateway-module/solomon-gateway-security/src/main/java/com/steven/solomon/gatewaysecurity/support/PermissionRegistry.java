package com.steven.solomon.gatewaysecurity.support;

import com.steven.solomon.gatewaysecurity.model.PermissionDefinition;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** 保存本服务扫描到的接口权限，供 Swagger 增强和持久化同步使用。 */
public class PermissionRegistry {

  private final List<PermissionDefinition> permissions = new CopyOnWriteArrayList<>();

  public void replaceAll(Collection<PermissionDefinition> permissions) {
    this.permissions.clear();
    this.permissions.addAll(permissions);
  }

  public List<PermissionDefinition> findAll() {
    return List.copyOf(permissions);
  }
}
