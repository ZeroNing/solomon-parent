package com.steven.solomon.aspect;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.pojo.enums.SwitchModeEnum;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
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
        if(!enabled){
            logger.info("PowerJob组件不启用");
            return point.proceed();
        }
        // 获取方法参数
        Object[] args = point.getArgs();
        TaskContext taskContext = null;
        if(ValidateUtils.isNotEmpty(args)){
            for(Object arg : args){
                taskContext = (TaskContext) arg;
            }
        }
        if(ValidateUtils.isNotEmpty(taskContext)){
            logger.info("当前任务id:{},任务实例ID:{},子任务实例ID:{},任务参数:{}",taskContext.getJobId(),taskContext.getInstanceId(),taskContext.getSubInstanceId(),taskContext.getJobParams());
        }
        return point.proceed();
    }
}
