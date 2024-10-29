package com.steven.solomon.consumer;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.date.StopWatch;
import com.steven.solomon.annotation.JobTask;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;

public abstract class AbstractJobConsumer<R> {

    protected final Logger logger = LoggerUtils.logger(getClass());

    private final XxlJob xxlJob = getClass().getAnnotation(XxlJob.class);

    public ReturnT<R> onMessage(){
        logger.info("BeanName:{},开启任务调度",getXxlJobBeanName());
        StopWatch stopWatch = new StopWatch();
        ReturnT<R> result = null;
        try {
            stopWatch.start();
            result = handle();
        }catch(Throwable e){
            logger.error("AbstractJobConsumer:调度报错 异常为:", e);
            saveLog(e);
        } finally {
            stopWatch.stop();
            Double second      = Double.parseDouble(String.valueOf(stopWatch.getLastTaskTimeMillis())) / 1000;
            logger.info("BeanName:{},结束任务调度,耗时:{}秒",getXxlJobBeanName(),second);
        }
        return result;
    }

    public abstract ReturnT<R> handle();

    public String getXxlJobBeanName(){
        return ValidateUtils.isNotEmpty(xxlJob) ? xxlJob.value() : "";
    }

    /**
     * 保存消费失败的消息
     */
    public abstract void saveLog(Throwable throwable);
}
