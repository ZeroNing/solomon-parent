package com.steven.solomon.security.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 内存权限存储默认实现。
 *
 * <p>将权限元数据保存在内存列表中，适合单机部署或开发测试环境。
 * 生产环境建议替换为持久化实现。</p>
 *
 * @author steven
 */
public class InMemoryPermissionStore implements PermissionStore {

    private final List<PermissionInfo> permissions = new CopyOnWriteArrayList<>();

    @Override
    public void saveAll(List<PermissionInfo> permissions) {
        if (CollUtil.isEmpty(permissions)) {
            return;
        }
        this.permissions.clear();
        this.permissions.addAll(permissions);
    }

    @Override
    public List<PermissionInfo> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(permissions));
    }

    @Override
    public List<String> findAnonymousPaths() {
        List<String> paths = new ArrayList<>();
        for (PermissionInfo info : permissions) {
            if (info.anonymous() && StrUtil.isNotBlank(info.path())) {
                paths.add(info.path());
            }
        }
        return paths;
    }
}
