package com.steven.solomon.security.core.permission;

import java.util.List;

/**
 * 权限存储（SPI 接口，可替换实现）。
 *
 * <p>SDK 默认提供 {@link InMemoryPermissionStore} 内存实现。
 * 客户可替换为数据库或配置中心实现。</p>
 *
 * @author steven
 */
public interface PermissionStore {

    /**
     * 批量保存权限元数据。
     *
     * @param permissions 权限元数据列表
     */
    void saveAll(List<PermissionInfo> permissions);

    /**
     * 返回所有已注册的权限元数据。
     *
     * @return 权限列表；无数据时返回空列表
     */
    List<PermissionInfo> findAll();

    /**
     * 返回所有标记为匿名的接口路径。
     *
     * @return 匿名路径列表
     */
    List<String> findAnonymousPaths();
}
