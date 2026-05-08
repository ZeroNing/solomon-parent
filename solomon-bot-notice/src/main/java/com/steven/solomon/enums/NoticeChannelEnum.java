package com.steven.solomon.enums;

/**
 * 通知渠道枚举
 * 支持三大主流办公机器人
 */
public enum NoticeChannelEnum {

    /** 企业微信机器人 */
    WECHAT_WORK("wechat_work", "企业微信"),

    /** 钉钉机器人 */
    DING_TALK("ding_talk", "钉钉"),

    /** 飞书机器人 */
    FEISHU("feishu", "飞书");

    private final String code;
    private final String name;

    NoticeChannelEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
