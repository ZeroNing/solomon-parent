package com.steven.solomon.enums;


import com.steven.solomon.pojo.enums.BaseEnum;

/**
 * ProcessorType 枚举定义了不同类型的处理器。
 * 这个枚举实现了 BaseEnum 接口，以提供特定的枚举功能。
 */
public enum ProcessorType implements BaseEnum<Integer> {

    /**
     * Built-in processor type.
     * 内建处理器：系统默认包含的处理器。
     */
    BUILT_IN(1, "内建处理器"),

    /**
     * External processor type.
     * 外部处理器（动态加载）：可以动态加载的外部处理器。
     */
    EXTERNAL(4, "外部处理器（动态加载）");

    // 用于存储处理器类型的整数标签
    private final Integer label;

    // 用于存储处理器类型的描述
    private final String desc;

    /**
     * 构造函数，初始化枚举值的标签和描述。
     *
     * @param label 用于标识处理器类型的整数标签。
     * @param desc  用于描述处理器类型的文本信息。
     */
    ProcessorType(Integer label, String desc) {
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

