package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

/**
 * @author xuxueli 2020-10-29 21:11:23
 */
public enum MisfireStrategyEnum  implements BaseEnum<String> {

    /**
     * do nothing
     */
    DO_NOTHING("忽略"),

    /**
     * fire once now
     */
    FIRE_ONCE_NOW("立即执行一次");

    private final String desc;

    MisfireStrategyEnum(String desc) {
        this.desc = desc;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public String label() {
        return this.name();
    }

    @Override
    public String key() {
        return this.name();
    }

}
