package com.steven.solomon.enums;

/**
 * 通知消息类型枚举。
 *
 * <p>各平台能力不完全一致，不支持的类型由服务实现降级为 Markdown 或平台卡片。</p>
 */
public enum NoticeMsgTypeEnum {

    /** 纯文本消息，支持 @。 */
    TEXT("text", "纯文本"),

    /** Markdown 富文本消息，支持 @。 */
    MARKDOWN("markdown", "Markdown"),

    /** 链接卡片消息。 */
    LINK("link", "链接消息"),

    /** 文件消息，需要平台媒体资源标识。 */
    FILE("file", "文件消息"),

    /** 图片消息，需要平台媒体资源标识。 */
    IMAGE("image", "图片消息"),

    /** 语音消息，需要平台媒体资源标识。 */
    VOICE("voice", "语音消息"),

    /** 通用卡片消息。 */
    CARD("card", "卡片消息"),

    /** 交互卡片消息。 */
    ACTION_CARD("actionCard", "交互卡片"),

    /** Feed 流卡片，主要用于钉钉。 */
    FEED_CARD("feedCard", "Feed 流卡片"),

    /** 群名片，主要用于飞书。 */
    SHARE_CHAT("share_chat", "群名片");

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
