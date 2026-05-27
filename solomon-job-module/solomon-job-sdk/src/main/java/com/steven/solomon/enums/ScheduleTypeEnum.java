package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

/**
 * @author xuxueli 2020-10-29 21:11:23
 */
public enum ScheduleTypeEnum implements BaseEnum<String> {

    NONE("不调度"),

    /**
     * schedule by cron
     */
    CRON("CRON调度"),

    /**
     * schedule by fixed rate (in seconds)
     */
    FIX_RATE("固定速率"),

    /**
     * schedule by fix delay (in seconds)， after the last time
     */
    /*FIX_DELAY(I18nUtil.getString("schedule_type_fix_delay"))*/;

    private final String desc;

    ScheduleTypeEnum(String desc) {
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
