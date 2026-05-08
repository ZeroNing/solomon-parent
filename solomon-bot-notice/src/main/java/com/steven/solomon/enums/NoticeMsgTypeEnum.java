package com.steven.solomon.enums;

/**
 * 通知消息类型枚举
 * 不同平台支持的类型不同，不支持的类型会自动降级为Markdown
 */
public enum NoticeMsgTypeEnum {

    /** 纯文本消息，所有平台支持
     * 支持@功能
     */
    TEXT("text", "纯文本"),

    /**
     * Markdown富文本消息，所有平台支持
     * 支持@功能，支持Markdown语法
     */
    MARKDOWN("markdown", "Markdown格式"),

    /**
     * 链接卡片消息，所有平台支持
     * 需要设置linkUrl（跳转地址）和linkPicUrl（封面图）
     */
    LINK("link", "链接消息"),

    /**
     * 文件消息，所有平台支持
     * 需要先上传文件获取media_id
     */
    FILE("file", "文件消息"),

    /**
     * 图片消息，所有平台支持
     * 需要先上传图片获取media_id
     */
    IMAGE("image", "图片消息"),

    /**
     * 语音消息，企业微信/钉钉支持，飞书不支持
     * 需要先上传语音文件获取media_id
     */
    VOICE("voice", "语音消息"),

    /**
     * 通用卡片消息
     */
    CARD("card", "卡片消息"),

    /**
     * 交互卡片消息，所有平台支持
     * 支持添加按钮，点击跳转指定链接
     */
    ACTION_CARD("actionCard", "交互卡片"),

    /**
     * Feed流多图文消息，仅钉钉支持
     * 支持多个图文条目
     */
    FEED_CARD("feedCard", "Feed流卡片");

    private final String code;
    private final String desc;

    NoticeMsgTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
