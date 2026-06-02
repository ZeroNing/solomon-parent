package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

/**
 * XXL-JOB 调度类型枚举。
 *
 * <p>定义任务的调度触发方式。
 * 对应 XXL-JOB 调度中心的「调度类型」选项。</p>
 *
 * @author xuxueli 2020-10-29 21:11:23
 */
public enum ScheduleTypeEnum implements BaseEnum<String> {

    /**
     * 不调度：任务不会自动触发，仅通过 API 手动触发。
     */
    NONE("不调度"),

    /**
     * CRON 调度：使用 CRON 表达式定义调度时间规则。
     */
    CRON("CRON调度"),

    /**
     * 固定速率（秒）：以固定的时间间隔（秒为单位）重复调度。
     */
    FIX_RATE("固定速率"),

    /**
     * 固定延迟（秒）：上次执行完成后，延迟固定时间（秒）再执行下一次。
     */
    /*FIX_DELAY("固定延迟")*/;

    /** 调度类型的中文描述。 */
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
