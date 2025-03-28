package com.steven.handler;

import com.steven.solomon.annotation.JobTask;
import com.steven.solomon.consumer.AbstractJobConsumer;

@JobTask(tenantCode = "1")
public class TestHandler2 extends AbstractJobConsumer {

    @Override
    public void handle(String jobParam) {
    }
}
