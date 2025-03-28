package com.steven.solomon.utils;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JobUtils {

    private Map<String, XxlJobSpringExecutor> jobExecutorMap = new ConcurrentHashMap<>();

    public Map<String, XxlJobSpringExecutor> getJobExecutorMap() {
        return jobExecutorMap;
    }

    public void putJobExecutorMap(String tenantCode,XxlJobSpringExecutor xxlJobSpringExecutor) {
        jobExecutorMap.put(tenantCode,xxlJobSpringExecutor);
    }
}
