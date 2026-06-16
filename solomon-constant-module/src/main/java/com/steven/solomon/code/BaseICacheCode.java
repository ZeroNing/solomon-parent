package com.steven.solomon.code;

/**
 * 缓存基础常量接口，定义分布式锁等缓存分组的编码。
 */
public interface BaseICacheCode {
    /**
     * 分布式锁接口redis分组
     */
    String REDIS_LOCK = "lock";
}
