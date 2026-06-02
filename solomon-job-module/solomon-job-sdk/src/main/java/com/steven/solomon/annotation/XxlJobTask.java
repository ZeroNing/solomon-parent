package com.steven.solomon.annotation;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.enums.ExecutorBlockStrategyEnum;
import com.steven.solomon.enums.ExecutorRouteStrategyEnum;
import com.steven.solomon.enums.GlueTypeEnum;
import com.steven.solomon.enums.MisfireStrategyEnum;
import com.steven.solomon.enums.ScheduleTypeEnum;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** XXL-JOB 专属任务参数。 */
@Retention(RetentionPolicy.RUNTIME)
public @interface XxlJobTask {

    /**
     * 任务所属执行器分组 ID。
     */
    int jobGroup() default 1;

    /**
     * 任务负责人。
     */
    String author() default StrUtil.EMPTY;

    /**
     * 报警邮件地址，多个用逗号分隔。
     */
    String alarmEmail() default StrUtil.EMPTY;

    /**
     * 调度类型，默认为固定速率。
     */
    ScheduleTypeEnum scheduleType() default ScheduleTypeEnum.FIX_RATE;

    /**
     * 调度配置，固定速率模式下单位为秒，默认 30 秒。
     */
    String scheduleConf() default "30";

    /**
     * 运行模式，默认为 Bean 模式。
     */
    GlueTypeEnum glueType() default GlueTypeEnum.BEAN;

    /**
     * 执行器任务 Handler 名称。
     */
    String executorHandler() default StrUtil.EMPTY;

    /**
     * 执行器任务参数。
     */
    String executorParam() default StrUtil.EMPTY;

    /**
     * 路由策略，默认为第一个。
     */
    ExecutorRouteStrategyEnum executorRouteStrategy() default ExecutorRouteStrategyEnum.FIRST;

    /**
     * 子任务 ID，多个用逗号分隔。
     */
    String childJobId() default StrUtil.EMPTY;

    /**
     * 调度过期策略，默认为忽略。
     */
    MisfireStrategyEnum misfireStrategy() default MisfireStrategyEnum.DO_NOTHING;

    /**
     * 阻塞处理策略，默认为串行执行。
     */
    ExecutorBlockStrategyEnum executorBlockStrategy() default ExecutorBlockStrategyEnum.SERIAL_EXECUTION;

    /**
     * 任务执行超时时间，单位秒，0 表示不限制。
     */
    int executorTimeout() default 0;

    /**
     * 失败重试次数。
     */
    int executorFailRetryCount() default 0;

    /**
     * 任务是否启用。
     */
    boolean start() default false;
}
