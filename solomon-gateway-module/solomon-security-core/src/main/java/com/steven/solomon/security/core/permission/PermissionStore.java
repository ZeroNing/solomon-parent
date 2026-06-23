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

    /**
     * Remove all cached/stored permission metadata.
     *
     * <p>Implementations backed by an external permission center should also invalidate any local permission
     * decision cache in this method.</p>
     */
    default void invalidateAll() {
        throw new UnsupportedOperationException("Permission cache invalidation is not supported");
    }

    /**
     * Remove one permission by code and invalidate related permission decision cache.
     *
     * @param code permission code
     * @return true if at least one permission entry was removed
     */
    default boolean invalidateByCode(String code) {
        throw new UnsupportedOperationException("Permission cache invalidation by code is not supported");
    }

    /**
     * Remove permissions bound to one HTTP path and invalidate related permission decision cache.
     *
     * @param path request path
     * @param method HTTP method, blank means all methods under the path
     * @return true if at least one permission entry was removed
     */
    default boolean invalidateByPath(String path, String method) {
        throw new UnsupportedOperationException("Permission cache invalidation by path is not supported");
    }

    /**
     * Monotonic version for cache consumers. A changed value means consumers should reload permission metadata.
     *
     * @return current permission metadata version
     */
    default long version() {
        return 0L;
    }
}
