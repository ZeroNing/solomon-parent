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

    TimeExpressionType timeExpressionType() default TimeExpressionType.FIXED_RATE;

    String timeExpression() default "30000";

    ExecuteType executeType() default ExecuteType.STANDALONE;

    ProcessorType processorType() default ProcessorType.BUILT_IN;

    String processorInfo() default StrUtil.EMPTY;

    int maxInstanceNum() default 0;

    int concurrency() default 0;

    long instanceTimeLimit() default 0L;

    int instanceRetryNum() default 0;

    int taskRetryNum() default 0;

    double minCpuCores() default 0;

    double minMemorySpace() default 0;

    double minDiskSpace() default 0;

    boolean enable() default true;

    DispatchStrategy dispatchStrategy() default DispatchStrategy.HEALTH_FIRST;

    String dispatchStrategyConfig() default StrUtil.EMPTY;

    String lifeCycleStart() default StrUtil.EMPTY;

    String lifeCycleEnd() default StrUtil.EMPTY;

    int alertThreshold() default 0;

    int statisticWindowLen() default 0;

    int silenceWindowLen() default 0;

    LogType type() default LogType.NULL;

    LogLevel level() default LogLevel.INFO;

    TaskTrackerBehavior taskTrackerBehavior() default TaskTrackerBehavior.NORMAL;
}
