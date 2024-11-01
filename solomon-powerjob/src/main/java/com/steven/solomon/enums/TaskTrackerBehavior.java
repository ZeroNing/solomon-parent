package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

/**
 * TaskTracker 行为枚举
 *
 * @author tjq
 * @since 2024/2/24
 */
/**
 * TaskTrackerBehavior 枚举定义了任务跟踪器的不同行为模式。
 * 这个枚举实现了 BaseEnum 接口，以提供特定的枚举功能。
 */
public enum TaskTrackerBehavior implements BaseEnum<Integer> {

    /**
     * Normal behavior.
     * 普通：不进行特殊处理，参与集群计算，可能会导致 TaskTracker 负载比常规节点高。
     * 适用于节点数不多且任务不繁重的场景。
     */
    NORMAL(1),

    /**
     * Paddling behavior.
     * 划水：只负责管理节点，不参与计算，以实现最佳稳定性。
     * 适用于节点数量非常多的大规模计算场景，牺牲一个计算节点以提升整体稳定性。
     */
    PADDLING(11);

    // 用于存储任务跟踪器行为类型的整数标签
    private final Integer label;

    /**
     * 构造函数，初始化枚举值的标签。
     *
     * @param label 用于标识任务跟踪器行为类型的整数标签。
     */
    TaskTrackerBehavior(Integer label) {
        this.label = label;
    }

    /**
     * 获取枚举值的描述，与枚举名称相同。
     *
     * @return 枚举的名称。
     */
    @Override
    public String getDesc() {
        return this.name();
    }

    /**
     * 获取枚举值的标签。
     *
     * @return 整数形式的标签。
     */
    @Override
    public Integer label() {
        return this.label;
    }

    /**
     * 获取枚举值的键，与枚举名称相同。
     *
     * @return 枚举的名称。
     */
    @Override
    public String key() {
        return this.name();
    }
}
