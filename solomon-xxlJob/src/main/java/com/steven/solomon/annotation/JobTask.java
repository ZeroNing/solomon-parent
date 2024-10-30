package com.steven.solomon.annotation;

import com.steven.solomon.enums.*;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * xxl-job注解
 */
@Target(value = { ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface JobTask {

    @AliasFor(annotation = Component.class)
    String value() default "";

    /**
     * 执行器主键ID
     */
    int jobGroup() default 1;
    /**
     * 任务描述
     */
    String taskName();

    /**
     * 负责人
     */
    String author();

    /**
     * 报警邮件
     */
    String alarmEmail() default "";

    /**
     * 调度类型 默认不调度
     */
    ScheduleTypeEnum scheduleType() default ScheduleTypeEnum.NONE;

    /**
     * 调度配置 CRON(* * * * * ?) FIX_RATE(30秒)
     */
    String scheduleConf() default "";

    /**
     * 运行模式
     */
    GlueTypeEnum glueType() default GlueTypeEnum.BEAN;

    /**
     * 执行器，任务Handler名称 默认:当前类的类名
     */
    String executorHandler() default "";

    /**
     * 执行器 任务参数
     */
    String executorParam() default "";

    /**
     * 路由策略
     */
    ExecutorRouteStrategyEnum executorRouteStrategy() default ExecutorRouteStrategyEnum.FIRST;

    /**
     * 子任务ID，多个逗号分隔
     */
    String childJobId() default "";

    /**
     * 调度过期策略
     */
    MisfireStrategyEnum misfireStrategy() default MisfireStrategyEnum.DO_NOTHING;

    /**
     * 阻塞处理策略
     */
    ExecutorBlockStrategyEnum executorBlockStrategy() default ExecutorBlockStrategyEnum.SERIAL_EXECUTION;

    /**
     * 任务执行超时时间，单位秒
     */
    int executorTimeout() default 0;

    /**
     * 失败重试次数
     */
    int executorFailRetryCount() default 0;

    /**
     * 是否启动
     */
    boolean start() default true;
}
