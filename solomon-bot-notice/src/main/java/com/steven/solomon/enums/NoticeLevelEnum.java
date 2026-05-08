package com.steven.solomon.enums;

/**
 * 通知级别枚举
 */
public enum NoticeLevelEnum {

    INFO("info", "普通", "🔵"),
    WARNING("warning", "警告", "🟡"),
    ERROR("error", "错误", "🔴"),
    SUCCESS("success", "成功", "🟢"),
    CRITICAL("critical", "严重", "🔥");

    private final String code;
    private final String name;
    private final String emoji;

    NoticeLevelEnum(String code, String name, String emoji) {
        this.code = code;
        this.name = name;
        this.emoji = emoji;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getEmoji() {
        return emoji;
    }
}
