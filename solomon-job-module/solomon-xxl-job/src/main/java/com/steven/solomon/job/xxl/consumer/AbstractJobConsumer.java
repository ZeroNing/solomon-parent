package com.steven.solomon.job.xxl.consumer;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import cn.hutool.core.date.StopWatch;
import com.steven.solomon.annotation.JobTask;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.IJobHandler;
import org.slf4j.Logger;

/**
 * XXL-JOB 抽象任务消费者。
 *
 * <p>继承 {@link IJobHandler} 提供任务执行的骨架模板，包含日志记录、
 * 执行耗时统计和异常处理。子类只需实现 {@link #handle(String)} 方法。</p>
 */
public abstract class AbstractJobConsumer extends IJobHandler {

    protected final Logger logger = LoggerUtils.logger(getClass());

    /** 当前类上的 {@link JobTask} 注解。 */
    private final JobTask jobTask = getClass().getAnnotation(JobTask.class);

    /**
     * XXL-JOB Bean 名称，优先使用注解中 {@link XxlJobTask#executorHandler()} 配置，
     * 未配置时回退为类名。
     */
    protected final String xxlJobBeanName = ObjectUtil.isNotEmpty(jobTask) ? StrUtil.blankToDefault(jobTask.xxlJob().executorHandler(),getClass().getSimpleName()) : getClass().getSimpleName();

    /**
     * 执行任务入口，由 XXL-JOB 调度中心触发。
     *
     * @throws Exception 任务执行异常
     */
    public void execute() throws Exception{
        String jobParam = XxlJobHelper.getJobParam();
        logger.info("BeanName:{},任务参数:{},开启任务调度",xxlJobBeanName,jobParam);
        StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            handle(jobParam);
        }catch (Throwable e) {
            logger.error("BeanName:{} AbstractJobConsumer:调度报错 异常为:",xxlJobBeanName, e);
            saveLog(jobParam,e);
            throw e;
        } finally {
            stopWatch.stop();
            Double second = Double.parseDouble(String.valueOf(stopWatch.getLastTaskTimeMillis())) / 1000;
            logger.info("BeanName:{},结束任务调度,耗时:{}秒",xxlJobBeanName,second);
        }
    }

    public abstract void handle(String jobParam) throws Exception;

    /**
     * 保存消费失败的消息
     */
    public void saveLog(String jobParam,Throwable throwable) {
        logger.error("BeanName:{},任务参数:{}.出现了异常,异常为:",xxlJobBeanName,jobParam,throwable);
    }
}
