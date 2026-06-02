package com.steven.solomon.gateway.permission;

import java.util.Collection;

/** 接口权限元数据存储扩展点。 */
public interface ApiPermissionStore {

  /** 保存扫描到的接口权限；引入模块可以替换为数据库或配置中心实现。 */
  void saveAll(Collection<ApiPermissionMetadata> permissions);
}
