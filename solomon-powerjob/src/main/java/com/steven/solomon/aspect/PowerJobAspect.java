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
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;

import java.io.PrintWriter;
import java.io.StringWriter;

@Aspect
@Configuration
public class PowerJobAspect {

    private final Logger logger = LoggerUtils.logger(getClass());

    @Value("${powerjob.worker.enabled: true}")
    private boolean enabled;

    //任务ID
    private String jobId = null;
    //任务实例ID
    private String instanceId = null;
    //子任务实例ID
    private String subInstanceId = null;
    //任务参数
    private String jobParams = null;

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
                if(arg instanceof TaskContext){
                    taskContext = (TaskContext) arg;
                }
            }
        }

        if(ValidateUtils.isNotEmpty(taskContext)){
            jobId = String.valueOf(taskContext.getJobId());
            instanceId = String.valueOf(taskContext.getInstanceId());
            subInstanceId = String.valueOf(taskContext.getSubInstanceId());
            jobParams = taskContext.getJobParams();
            logger.info("当前任务id:{},任务实例ID:{},子任务实例ID:{},任务参数:{}",jobId,instanceId,subInstanceId,jobParams);
        }
        Object result = null;
        try{
            result = point.proceed();
        } catch (Throwable e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            logger.info("当前任务id:{},任务实例ID:{},子任务实例ID:{},任务参数:{},出现了异常:",jobId,instanceId,subInstanceId,jobParams,e);
            return new ProcessResult(false,"出现异常:"+sw.toString());
        }
        return result;
    }
}
