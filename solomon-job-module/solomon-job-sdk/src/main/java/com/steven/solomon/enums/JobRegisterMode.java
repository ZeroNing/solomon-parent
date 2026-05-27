package com.steven.solomon.enums;

/**
 * 自动注册写入模式。
 */
public enum JobRegisterMode {

    /**
     * 只创建不存在的任务，已存在任务不更新。
     */
    CREATE_ONLY,

    /**
     * 只更新已存在的任务，不创建新任务。
     */
    UPDATE_ONLY,

    /**
     * 不存在则创建，已存在则更新。
     */
    UPSERT
}
