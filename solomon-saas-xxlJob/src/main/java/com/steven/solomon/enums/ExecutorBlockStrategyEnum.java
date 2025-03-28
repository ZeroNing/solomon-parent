package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

/**
 * Created by xuxueli on 17/5/9.
 */
public enum ExecutorBlockStrategyEnum implements BaseEnum<String> {

    SERIAL_EXECUTION("单机串行"),
    /*CONCURRENT_EXECUTION("并行"),*/
    DISCARD_LATER("丢弃后续调度"),
    COVER_EARLY("覆盖之前调度");


    private final String desc;

    ExecutorBlockStrategyEnum(String desc) {
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
