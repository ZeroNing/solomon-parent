package com.steven.handler;

import com.steven.solomon.annotation.JobTask;
import com.steven.solomon.consumer.AbstractJobConsumer;
import com.steven.solomon.enums.ScheduleTypeEnum;

@JobTask(taskName = "123456", author = "123456",scheduleType = ScheduleTypeEnum.FIX_RATE,scheduleConf = "1")
public class TestHandler extends AbstractJobConsumer {

    @Override
    public void handle(String jobParam) {
    }

}
