package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

/**
 * DispatchStrategy
 *
 * @author tjq
 * @since 2021/2/22
 */
/**
 * DispatchStrategy 枚举定义了不同的任务分发策略。
 * 这个枚举实现了 BaseEnum 接口，以提供特定的枚举功能。
 */
public enum DispatchStrategy implements BaseEnum<Integer> {

    /**
     * Health-first strategy.
     * 健康度优先：优先选择健康状态较好的节点进行任务分发。
     */
    HEALTH_FIRST(1),

    /**
     * Random strategy.
     * 随机：随机选择节点进行任务分发。
     */
    RANDOM(2),

    /**
     * Specify strategy.
     * 指定执行：指定特定节点进行任务分发。
     */
    SPECIFY(11);

    // 用于存储分发策略类型的整数标签
    private final Integer label;

    /**
     * 构造函数，初始化枚举值的标签。
     *
     * @param label 用于标识分发策略类型的整数标签。
     */
    DispatchStrategy(Integer label) {
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

