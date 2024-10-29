package com.steven.handler;

import com.steven.solomon.annotation.JobTask;
import com.steven.solomon.consumer.AbstractJobConsumer;
import com.steven.solomon.enums.ScheduleTypeEnum;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import groovyjarjarpicocli.CommandLine;

@JobTask(taskName = "123456", author = "123456", executorHandler = "testHandler",scheduleType = ScheduleTypeEnum.FIX_RATE,scheduleConf = "1")
public class TestHandler extends AbstractJobConsumer {

    @Override
    public void handle(String jobParam) {
    }

    @Override
    public void saveLog(Throwable throwable) {

    }
}
