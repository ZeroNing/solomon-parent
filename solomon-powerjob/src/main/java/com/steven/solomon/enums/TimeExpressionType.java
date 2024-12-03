package com.steven.solomon.enums;

import com.google.common.collect.Lists;
import com.steven.solomon.pojo.enums.BaseEnum;
import scala.Int;

import java.util.Collections;
import java.util.List;

/**
 * TimeExpressionType 枚举定义了不同的时间表达式类型，用于调度任务的触发机制。
 * 这个枚举实现了 BaseEnum 接口，以提供特定的枚举功能。
 */
public enum TimeExpressionType implements BaseEnum<Integer> {

    /**
     * API 触发类型，使用代码程序手动触发任务。
     */
    API(1),

    /**
     * CRON 表达式触发类型，使用类似于 Linux Cron 的表达式来调度任务。
     */
    CRON(2),

    /**
     * 固定频率触发类型，以固定的时间间隔重复执行任务。
     */
    FIXED_RATE(3),

    /**
     * 固定延迟触发类型，在上一次执行完成后等待固定时间再执行下一次任务。
     */
    FIXED_DELAY(4),

    /**
     * 工作流触发类型，任务作为工作流的一部分被调度。
     */
    WORKFLOW(5),

    /**
     * 每日时间间隔触发类型，在每天的某个时间间隔内定期触发任务。
     */
    DAILY_TIME_INTERVAL(11);

    // 用于存储时间表达式类型的整数标签
    private Integer label;

    /**
     * 构造函数，初始化枚举值的标签。
     *
     * @param label 用于标识时间表达式类型的整数标签。
     */
    TimeExpressionType(Integer label) {
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
