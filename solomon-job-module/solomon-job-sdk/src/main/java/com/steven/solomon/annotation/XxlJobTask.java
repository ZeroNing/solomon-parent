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

    int jobGroup() default 1;

    String author() default StrUtil.EMPTY;

    String alarmEmail() default StrUtil.EMPTY;

    ScheduleTypeEnum scheduleType() default ScheduleTypeEnum.NONE;

    String scheduleConf() default StrUtil.EMPTY;

    GlueTypeEnum glueType() default GlueTypeEnum.BEAN;

    String executorHandler() default StrUtil.EMPTY;

    String executorParam() default StrUtil.EMPTY;

    ExecutorRouteStrategyEnum executorRouteStrategy() default ExecutorRouteStrategyEnum.FIRST;

    String childJobId() default StrUtil.EMPTY;

    MisfireStrategyEnum misfireStrategy() default MisfireStrategyEnum.DO_NOTHING;

    ExecutorBlockStrategyEnum executorBlockStrategy() default ExecutorBlockStrategyEnum.SERIAL_EXECUTION;

    int executorTimeout() default 0;

    int executorFailRetryCount() default 0;

    boolean start() default false;
}
