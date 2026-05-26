package com.steven.solomon.enums;

/**
 * 通知级别枚举。
 */
public enum NoticeLevelEnum {

    /** 普通通知。 */
    INFO("info", "普通", "🔵"),

    /** 警告通知。 */
    WARNING("warning", "警告", "🟡"),

    /** 错误通知。 */
    ERROR("error", "错误", "🔴"),

    /** 成功通知。 */
    SUCCESS("success", "成功", "🟢"),

    /** 严重故障通知。 */
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
