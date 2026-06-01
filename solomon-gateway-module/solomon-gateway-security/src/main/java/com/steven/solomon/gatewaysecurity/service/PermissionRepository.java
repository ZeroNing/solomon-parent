package com.steven.solomon.gatewaysecurity.service;

import com.steven.solomon.gatewaysecurity.model.PermissionDefinition;
import java.util.Collection;

/**
 * 保存接口权限定义。
 *
 * <p>业务系统可以覆盖默认实现，将扫描结果保存到数据库、Redis 或配置中心。</p>
 */
@FunctionalInterface
public interface PermissionRepository {

  void saveAll(Collection<PermissionDefinition> permissions);
}
