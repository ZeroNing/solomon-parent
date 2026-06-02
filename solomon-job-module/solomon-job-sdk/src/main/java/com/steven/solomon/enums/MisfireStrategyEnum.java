package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

/**
 * XXL-JOB 调度过期策略枚举。
 *
 * <p>定义任务在调度过期时（即调度中心错过触发时间后的处理方式）。
 * 对应 XXL-JOB 调度中心的「调度过期策略」选项。</p>
 *
 * @author xuxueli 2020-10-29 21:11:23
 */
public enum MisfireStrategyEnum  implements BaseEnum<String> {

    /**
     * 忽略：错过调度周期后不执行，等待下一次正常调度。
     */
    DO_NOTHING("忽略"),

    /**
     * 立即执行一次：错过调度周期后立即触发一次补偿执行。
     */
    FIRE_ONCE_NOW("立即执行一次");

    /** 过期策略的中文描述。 */
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
