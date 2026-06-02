package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

/**
 * XXL-JOB 执行器路由策略枚举。
 *
 * <p>定义任务调度到执行器时选择具体执行器节点的策略。
 * 对应 XXL-JOB 调度中心的「路由策略」选项。</p>
 *
 * @author xuxueli 17/3/10
 */
public enum ExecutorRouteStrategyEnum implements BaseEnum<String> {

    /**
     * 第一个：固定选择第一个执行器。
     */
    FIRST("第一个"),
    /**
     * 最后一个：固定选择最后一个执行器。
     */
    LAST("最后一个"),
    /**
     * 轮询：依次选择每个执行器。
     */
    ROUND("轮询"),
    /**
     * 随机：随机选择一个执行器。
     */
    RANDOM("随机"),
    /**
     * 一致性 HASH：根据任务 ID 的 Hash 值选择固定的执行器。
     */
    CONSISTENT_HASH("一致性HASH"),
    /**
     * 最不经常使用：优先选择使用频率最低的执行器。
     */
    LEAST_FREQUENTLY_USED("最不经常使用"),
    /**
     * 最近最久未使用：优先选择最久未使用的执行器。
     */
    LEAST_RECENTLY_USED("最近最久未使用"),
    /**
     * 故障转移：检测执行器是否在线，跳过故障节点进行转移。
     */
    FAILOVER("故障转移"),
    /**
     * 忙碌转移：检测执行器繁忙程度，跳过繁忙节点进行转移。
     */
    BUSYOVER("忙碌转移"),
    /**
     * 分片广播：向所有执行器广播，执行器根据分片参数处理对应数据。
     */
    SHARDING_BROADCAST("分片广播");

    /** 路由策略的中文描述。 */
    private final String desc;

    ExecutorRouteStrategyEnum(String desc) {
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
