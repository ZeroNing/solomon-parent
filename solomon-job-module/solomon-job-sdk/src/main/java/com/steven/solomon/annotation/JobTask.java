package com.steven.solomon.annotation;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.enums.*;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 统一任务注解。
 * <p>
 * 同一个任务类可以同时声明 PowerJob 与 XXL-JOB 元数据，具体初始化器只读取自己平台需要的字段。
 */
@Target(value = { ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface JobTask {

    @AliasFor(annotation = Component.class)
    String value() default StrUtil.EMPTY;

    /**
     * 需要自动注册的平台。未配置时由实现模块按任务类型自动识别。
     */
    JobPlatform[] platforms() default {};

    /**
     * 任务名称。未配置时由各平台使用当前类名兜底。
     */
    String taskName() default StrUtil.EMPTY;

    /**
     * 任务描述
     */
    String taskDesc() default StrUtil.EMPTY;

    /**
     * 任务参数
     */
    String taskParams() default StrUtil.EMPTY;

    /**
     * 时间表达式类型 默认:固定频率
     */
    TimeExpressionType timeExpressionType() default TimeExpressionType.FIXED_RATE;

    /**
     * 时间表达式值 根据类型来配置
     */
    String timeExpression() default "30000";

    /**
     * 执行类型 默认:单机执行
     */
    ExecuteType executeType() default ExecuteType.STANDALONE;

    /**
     * 处理器类型 默认:内置
     */
    ProcessorType processorType() default ProcessorType.BUILT_IN;

    /**
     * 处理器信息。 默认只有是内置的情况下才拿当前class名字
     */
    String processorInfo() default StrUtil.EMPTY;

    /**
     * 最大实例数设置
     */
    int maxInstanceNum() default 0;

    /**
     * 并发设置
     */
    int concurrency() default 0;

    /**
     * 实例运行时间限制。{@code 0L}表示没有限制。
     */
    long instanceTimeLimit() default 0L;

    /**
     * 实例重试次数设置。
     */
    int instanceRetryNum() default 0;

    /**
     * 任务重试次数设置
     */
    int taskRetryNum() default 0;

    /**
     * 最小 CPU 要求。{@code 0}表示没有限制
     */
    double minCpuCores()default 0;

    /**
     * 最小内存要求，以 GB 为单位。
     */
    double minMemorySpace() default 0;

    /**
     * 最小磁盘空间，以 GB 为单位。{@code 0}表示没有限制。
     */
    double minDiskSpace() default 0;

    /**
     * 是否启用作业。
     */
    boolean enable() default true;

    /**
     * 派发策略。
     */
    DispatchStrategy dispatchStrategy() default DispatchStrategy.HEALTH_FIRST;

    /**
     * 某种派发策略背后的具体配置，值取决于 dispatchStrategy。
     */
    String dispatchStrategyConfig() default StrUtil.EMPTY;

    /**
     * 生命周期开始时间 格式"yyyy-MM-dd HH:mm:ss"
     */
    String lifeCycleStart() default StrUtil.EMPTY;

    /**
     * 生命周期结束时间 格式"yyyy-MM-dd HH:mm:ss"
     */
    String lifeCycleEnd() default StrUtil.EMPTY;

    /**
     * 获取告警阈值。
     *
     * @return 告警阈值的整数表示。默认值为 0，表示未设置具体阈值。
     */
    int alertThreshold() default 0;

    /**
     * 获取统计窗口长度。
     *
     * @return 统计窗口的长度，以整数形式表示。默认值为 0，表示未设置具体长度。
     */
    int statisticWindowLen() default 0;

    /**
     * 获取静默窗口长度。
     *
     * @return 静默窗口的长度，以整数形式表示。默认值为 0，表示未设置具体长度。
     */
    int silenceWindowLen() default 0;

    /**
     * 日志配置 默认不配置日志
     */
    LogType type() default LogType.NULL;

    /**
     * 日志等级 默认:INFO
     */
    LogLevel level() default LogLevel.INFO;

    /**
     * 高级配置
     */
    TaskTrackerBehavior taskTrackerBehavior() default TaskTrackerBehavior.NORMAL;

    /**
     * XXL-JOB 执行器主键 ID。
     */
    int jobGroup() default 1;

    /**
     * XXL-JOB 负责人。默认使用 spring.application.name，仍为空时使用当前类名。
     */
    String author() default StrUtil.EMPTY;

    /**
     * XXL-JOB 报警邮件。
     */
    String alarmEmail() default StrUtil.EMPTY;

    /**
     * XXL-JOB 调度类型。
     */
    ScheduleTypeEnum scheduleType() default ScheduleTypeEnum.NONE;

    /**
     * XXL-JOB 调度表达式。
     */
    String scheduleConf() default StrUtil.EMPTY;

    /**
     * XXL-JOB 运行模式。
     */
    GlueTypeEnum glueType() default GlueTypeEnum.BEAN;

    /**
     * XXL-JOB Handler 名称。未配置时使用当前类名。
     */
    String executorHandler() default StrUtil.EMPTY;

    /**
     * XXL-JOB 执行参数。
     */
    String executorParam() default StrUtil.EMPTY;

    /**
     * XXL-JOB 路由策略。
     */
    ExecutorRouteStrategyEnum executorRouteStrategy() default ExecutorRouteStrategyEnum.FIRST;

    /**
     * XXL-JOB 子任务 ID，多个使用英文逗号分隔。
     */
    String childJobId() default StrUtil.EMPTY;

    /**
     * XXL-JOB 调度过期策略。
     */
    MisfireStrategyEnum misfireStrategy() default MisfireStrategyEnum.DO_NOTHING;

    /**
     * XXL-JOB 阻塞处理策略。
     */
    ExecutorBlockStrategyEnum executorBlockStrategy() default ExecutorBlockStrategyEnum.SERIAL_EXECUTION;

    /**
     * XXL-JOB 超时时间，单位秒。
     */
    int executorTimeout() default 0;

    /**
     * XXL-JOB 失败重试次数。
     */
    int executorFailRetryCount() default 0;

    /**
     * XXL-JOB 自动注册后是否启动任务。
     */
    boolean start() default false;
}
