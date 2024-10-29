package com.steven.solomon.consumer;

import cn.hutool.core.date.StopWatch;
import com.steven.solomon.annotation.JobTask;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;

public abstract class AbstractJobConsumer extends IJobHandler {

    protected final Logger logger = LoggerUtils.logger(getClass());

    private final JobTask jobTask = getClass().getAnnotation(JobTask.class);

    public void execute() throws Exception{
        String jobParam = XxlJobHelper.getJobParam();
        logger.info("BeanName:{},任务参数:{},开启任务调度",getXxlJobBeanName(),jobParam);
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            handle(jobParam);
        }catch(Throwable e){
            logger.error("AbstractJobConsumer:调度报错 异常为:", e);
            saveLog(e);
            throw e;
        } finally {
            stopWatch.stop();
            Double second      = Double.parseDouble(String.valueOf(stopWatch.getLastTaskTimeMillis())) / 1000;
            logger.info("BeanName:{},结束任务调度,耗时:{}秒",getXxlJobBeanName(),second);
        }
    }

    public abstract void handle(String jobParam);

    public String getXxlJobBeanName(){
        return ValidateUtils.isNotEmpty(jobTask) ? jobTask.executorHandler() : "";
    }

    /**
     * 保存消费失败的消息
     */
    public abstract void saveLog(Throwable throwable);
}
