package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

/**
 * XXL-JOB 执行器阻塞处理策略枚举。
 *
 * <p>定义当任务调度到达但执行器还在处理上一次任务时的处理策略。
 * 对应 XXL-JOB 调度中心的「阻塞处理策略」选项。</p>
 *
 * @author xuxueli 17/5/9
 */
public enum ExecutorBlockStrategyEnum implements BaseEnum<String> {

    /**
     * 单机串行：任务请求进入单机执行器后以串行方式运行，后续调度任务排队等待。
     */
    SERIAL_EXECUTION("单机串行"),
    /*CONCURRENT_EXECUTION("并行"),*/
    /**
     * 丢弃后续调度：当执行器正在处理任务时，后续调度直接丢弃。
     */
    DISCARD_LATER("丢弃后续调度"),
    /**
     * 覆盖之前调度：当执行器正在处理任务时，终止当前运行并执行新的调度。
     */
    COVER_EARLY("覆盖之前调度");

    /** 阻塞策略的中文描述。 */
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
