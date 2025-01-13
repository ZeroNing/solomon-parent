package com.steven.solomon.pojo.enums;

public enum BotClientEnum implements BaseEnum<String>{
    WECHAT("wechat","微信"),
    DING_TALK("dingtalk","钉钉"),;

    private String label;

    private String desc;

    BotClientEnum(String label,String desc) {
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
