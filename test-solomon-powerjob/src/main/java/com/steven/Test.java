package com.steven;

import com.steven.solomon.annotation.JobTask;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

@JobTask(taskName = "5555")
public class Test implements BasicProcessor {
    @Override
    public ProcessResult process(TaskContext taskContext) throws Exception {

        return null;
    }
}
