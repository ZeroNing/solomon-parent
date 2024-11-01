package com.steven.solomon.enums;

import com.steven.solomon.pojo.enums.BaseEnum;

import java.util.Objects;

/**
 * 日志级别
 *
 * @author tjq
 * @since 12/20/20
 */
/**
 * LogLevel 枚举定义了日志记录的不同级别。
 * 这个枚举实现了 BaseEnum 接口，以提供特定的枚举功能。
 */
public enum LogLevel implements BaseEnum<Integer> {

    /**
     * Debug level.
     * DEBUG：用于开发和调试阶段，记录详细的信息。
     */
    DEBUG(1),

    /**
     * Info level.
     * INFO：记录一般的运行信息，反映正常的系统状态。
     */
    INFO(2),

    /**
     * Warning level.
     * WARN：记录潜在的问题，提示可能需要注意的情况。
     */
    WARN(3),

    /**
     * Error level.
     * ERROR：记录错误事件，表示程序运行出现问题。
     */
    ERROR(4),

    /**
     * Off level.
     * OFF：关闭日志记录，不输出任何日志。
     */
    OFF(99);

    // 用于存储日志级别的整数标签
    private final Integer label;

    /**
     * 构造函数，初始化枚举值的标签。
     *
     * @param label 用于标识日志级别的整数标签。
     */
    LogLevel(Integer label) {
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

