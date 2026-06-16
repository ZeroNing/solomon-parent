package com.steven.solomon.gateway.permission;

import java.util.List;

/**
 * 权限存储（SPI 接口，可替换实现）。
 *
 * <p>SDK 默认提供 {@link InMemoryPermissionStore} 内存实现，适合单机部署。
 * 微服务场景客户可替换为数据库或配置中心实现，使网关能加载到下游服务同步的权限目录。</p>
 *
 * @author steven
 */
public interface PermissionStore {

    /**
     * 批量保存权限元数据。
     *
     * <p>通常由 {@link PermissionScanner} 在启动扫描后调用，将扫描结果持久化。</p>
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
     * <p>网关据此放行无需 Token 的请求。</p>
     *
     * @return 匿名路径列表；无匿名接口时返回空列表
     */
    List<String> findAnonymousPaths();
}
