package com.steven.handler;

import com.steven.solomon.annotation.JobTask;
import com.steven.solomon.consumer.AbstractJobConsumer;
import com.steven.solomon.enums.ScheduleTypeEnum;

@JobTask(scheduleType = ScheduleTypeEnum.FIX_RATE,scheduleConf = "1")
public class TestHandler1 extends AbstractJobConsumer {

    @Override
    public void handle(String jobParam) {
    }

    @Override
    public void saveLog(Throwable throwable) {

    }
}
