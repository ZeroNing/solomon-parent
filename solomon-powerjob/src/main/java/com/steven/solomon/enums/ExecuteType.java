package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

/**
 * ExecuteType 枚举定义了不同的任务执行类型。
 * 这个枚举实现了 BaseEnum 接口，以提供特定的枚举功能。
 */
public enum ExecuteType implements BaseEnum<Integer> {

    /**
     * Standalone type of task.
     * 单机执行：任务在单个节点上执行。
     */
    STANDALONE(1, "单机执行"),

    /**
     * Broadcast type of task.
     * 广播执行：任务在所有可用节点上同时执行。
     */
    BROADCAST(2, "广播执行"),

    /**
     * MapReduce type of task.
     * MapReduce 执行：任务采用 MapReduce 模式进行分布式执行。
     */
    MAP_REDUCE(3, "MapReduce"),

    /**
     * Map type of task.
     * Map 执行：任务的执行逻辑类似于 Map 阶段。
     */
    MAP(4, "Map");

    // 用于存储执行类型的整数标签
    private final Integer label;

    // 用于存储执行类型的描述
    private final String desc;

    /**
     * 构造函数，初始化枚举值的标签和描述。
     *
     * @param label 用于标识任务执行类型的整数标签。
     * @param desc  用于描述任务执行类型的文本信息。
     */
    ExecuteType(Integer label, String desc) {
        this.label = label;
        this.desc = desc;
    }

    /**
     * 获取枚举值的描述，与枚举描述字段相同。
     *
     * @return 枚举的描述信息。
     */
    @Override
    public String getDesc() {
        return this.desc;
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

