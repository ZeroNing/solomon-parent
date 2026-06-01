package com.steven.solomon.aspect;

import cn.hutool.core.util.ObjectUtil;

import com.steven.solomon.utils.logger.LoggerUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;

@Aspect
@Configuration
public class PowerJobAspect {

    private final Logger logger = LoggerUtils.logger(getClass());

    @Value("${powerjob.worker.enabled: true}")
    private boolean enabled;

    @Pointcut("execution(* tech.powerjob.worker.core.processor.sdk.BasicProcessor.process(..))")
    void cutPoint() {}

    @Around("cutPoint()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        if (!enabled) {
            logger.info("PowerJob组件不启用");
            return point.proceed();
        }
        // 获取方法参数
        Object[] args = point.getArgs();
        TaskContext taskContext = null;
        if (ObjectUtil.isNotEmpty(args)) {
            for (Object arg : args) {
                if (arg instanceof TaskContext) {
                    taskContext = (TaskContext) arg;
                }
            }
        }

        if (ObjectUtil.isNotEmpty(taskContext)) {
            String jobId = String.valueOf(taskContext.getJobId());
            String instanceId = String.valueOf(taskContext.getInstanceId());
            String subInstanceId = String.valueOf(taskContext.getSubInstanceId());
            String jobParams = taskContext.getJobParams();
            logger.info("当前任务id:{},任务实例ID:{},子任务实例ID:{},任务参数:{}",jobId,instanceId,subInstanceId,jobParams);
        }
        Object result;
        try{
            result = point.proceed();
        } catch (Throwable e) {
            String jobId = ObjectUtil.isEmpty(taskContext) ? "" : String.valueOf(taskContext.getJobId());
            String instanceId = ObjectUtil.isEmpty(taskContext) ? "" : String.valueOf(taskContext.getInstanceId());
            String subInstanceId = ObjectUtil.isEmpty(taskContext) ? "" : String.valueOf(taskContext.getSubInstanceId());
            String jobParams = ObjectUtil.isEmpty(taskContext) ? "" : taskContext.getJobParams();
            logger.error("当前任务id:{},任务实例ID:{},子任务实例ID:{},任务参数:{},出现了异常", jobId, instanceId, subInstanceId, jobParams, e);
            return new ProcessResult(false, "出现异常:" + e.getMessage());
        }
        return result;
    }
}
