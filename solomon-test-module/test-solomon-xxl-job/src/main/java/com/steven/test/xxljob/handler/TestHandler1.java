package com.steven.test.xxljob.handler;

import com.steven.solomon.annotation.JobTask;
import com.steven.solomon.annotation.XxlJobTask;
import com.steven.solomon.job.xxl.consumer.AbstractJobConsumer;
import com.steven.solomon.enums.ScheduleTypeEnum;

@JobTask(xxlJob = @XxlJobTask(scheduleType = ScheduleTypeEnum.FIX_RATE, scheduleConf = "1"))
public class TestHandler1 extends AbstractJobConsumer {

    @Override
    public void handle(String jobParam) {
    }

}
