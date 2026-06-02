package com.steven.solomon.annotation;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.enums.DispatchStrategy;
import com.steven.solomon.enums.ExecuteType;
import com.steven.solomon.enums.LogLevel;
import com.steven.solomon.enums.LogType;
import com.steven.solomon.enums.ProcessorType;
import com.steven.solomon.enums.TaskTrackerBehavior;
import com.steven.solomon.enums.TimeExpressionType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** PowerJob 专属任务参数。 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PowerJobTask {

    /**
     * 时间表达式类型，默认为固定频率。
     */
    TimeExpressionType timeExpressionType() default TimeExpressionType.FIXED_RATE;

    /**
     * 时间表达式，固定频率模式下默认为 30 秒。
     */
    String timeExpression() default "30000";

    /**
     * 执行类型，默认为单机执行。
     */
    ExecuteType executeType() default ExecuteType.STANDALONE;

    /**
     * 处理器类型，默认为内置处理器。
     */
    ProcessorType processorType() default ProcessorType.BUILT_IN;

    /**
     * 处理器信息。
     */
    String processorInfo() default StrUtil.EMPTY;

    /**
     * 最大实例数，0 表示不限制。
     */
    int maxInstanceNum() default 1;

    /**
     * 最大并发数，0 表示不限制。
     */
    int concurrency() default 0;

    /**
     * 任务实例运行时间限制，单位毫秒，0 表示不限制。
     */
    long instanceTimeLimit() default 0L;

    /**
     * 实例重试次数。
     */
    int instanceRetryNum() default 0;

    /**
     * 任务重试次数。
     */
    int taskRetryNum() default 0;

    /**
     * 最低 CPU 核心数要求，0 表示不限制。
     */
    double minCpuCores() default 0;

    /**
     * 最低内存空间要求，单位 GB，0 表示不限制。
     */
    double minMemorySpace() default 0;

    /**
     * 最低磁盘空间要求，单位 GB，0 表示不限制。
     */
    double minDiskSpace() default 0;

    /**
     * 是否启用。
     */
    boolean enable() default true;

    /**
     * 调度策略，默认为健康优先。
     */
    DispatchStrategy dispatchStrategy() default DispatchStrategy.HEALTH_FIRST;

    /**
     * 调度策略配置，如安全阈值等。
     */
    String dispatchStrategyConfig() default StrUtil.EMPTY;

    /**
     * 生命周期起始时间，为空表示不限制。
     */
    String lifeCycleStart() default StrUtil.EMPTY;

    /**
     * 生命周期结束时间，为空表示不限制。
     */
    String lifeCycleEnd() default StrUtil.EMPTY;

    /**
     * 报警阈值，0 表示不报警。
     */
    int alertThreshold() default 0;

    /**
     * 统计窗口长度，单位分钟。
     */
    int statisticWindowLen() default 0;

    /**
     * 静默窗口长度，单位分钟。
     */
    int silenceWindowLen() default 0;

    /**
     * 日志类型。
     */
    LogType type() default LogType.NULL;

    /**
     * 日志级别，默认为 INFO。
     */
    LogLevel level() default LogLevel.INFO;

    /**
     * 任务追踪器行为，默认为正常。
     */
    TaskTrackerBehavior taskTrackerBehavior() default TaskTrackerBehavior.NORMAL;
}
