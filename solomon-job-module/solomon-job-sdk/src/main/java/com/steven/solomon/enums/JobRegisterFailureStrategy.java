package com.steven.solomon.enums;

/**
 * 自动注册失败处理策略。
 */
public enum JobRegisterFailureStrategy {

    /**
     * 注册失败时中断应用启动。
     */
    FAIL_FAST,

    /**
     * 注册失败时只记录警告，应用继续启动。
     */
    WARN_ONLY
}
