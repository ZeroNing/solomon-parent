package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

/**
 * Redis消息队列主题模式枚举。
 *
 * <p>定义消息监听的模式类型：</p>
 * <ul>
 *   <li>CHANNEL - 精确匹配频道名，直接订阅指定频道</li>
 *   <li>PATTERN - 模式匹配，支持通配符订阅多个频道</li>
 * </ul>
 */
public enum TopicMode implements BaseEnum<String> {

    /**
     * 直连模式，精确匹配频道名。
     */
    CHANNEL("CHANNEL","直连模式"),

    /**
     * 主题模式，支持通配符匹配。
     */
    PATTERN("PATTERN","主题模式"),;

    /** 标签值 */
    private final String label;

    /** 描述信息 */
    private final String desc;

    TopicMode(String label,String desc) {
        this.label = label;
        this.desc = desc;
    }

    /**
     * 获取描述信息。
     * @return 描述文本
     */
    @Override
    public String getDesc() {
        return desc;
    }

    /**
     * 获取标签值。
     * @return 标签
     */
    @Override
    public String label() {
        return this.label;
    }

    /**
     * 以枚举名称作为键。
     * @return 枚举名称
     */
    @Override
    public String key() {
        return this.name();
    }
}
