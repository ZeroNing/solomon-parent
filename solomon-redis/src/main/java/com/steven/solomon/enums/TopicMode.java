package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

public enum TopicMode implements BaseEnum<String> {
    CHANNEL("CHANNEL","直连模式"),
    PATTERN("PATTERN","主题模式"),;

    private final String label;

    private final String desc;

    TopicMode(String label,String desc) {
        this.label = label;
        this.desc = desc;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public String label() {
        return this.label;
    }

    @Override
    public String key() {
        return this.name();
    }
}
