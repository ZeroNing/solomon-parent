package com.steven.solomon.init;

import cn.hutool.core.annotation.AnnotationUtil;
import com.steven.solomon.annotation.JobTask;
import com.steven.solomon.entity.XxlJobInfo;
import com.steven.solomon.enums.ScheduleTypeEnum;
import com.steven.solomon.lambda.Lambda;
import com.steven.solomon.properties.XxlJobProperties;
import com.steven.solomon.service.CommonXxlJobService;
import com.steven.solomon.service.XxlJobService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.verification.ValidateUtils;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.handler.IJobHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Configuration
@Import(XxlJobProperties.class)
public class XxlJobInit extends AbstractMessageLineRunner<JobTask> {

    private final XxlJobProperties profile;

    private final XxlJobService service;

    public XxlJobInit(ApplicationContext applicationContext, XxlJobProperties profile, XxlJobService service) {
        SpringUtil.setContext(applicationContext);
        this.service = service;
        this.profile = profile;

    }

    @Override
    public void init(List<Object> clazzList) throws Exception {
        if(!profile.getEnabled()){
            logger.error("xxl-Job不启用,不初始化定时任务");
            return;
        }
        if(!profile.getAutoRegister()){
            logger.error("xxl-Job启用,但是配置了不允许自动注册");
            return;
        }
        String cookie = service.login();
        List<XxlJobInfo> xxlJobInfoList = service.findByExecutorHandler(cookie,"");
        Map<String,XxlJobInfo> xxlJobInfoMap = Lambda.toMap(xxlJobInfoList, XxlJobInfo::getExecutorHandler);
        for(Object obj : clazzList){
            Class<?> clazz = obj.getClass();
            JobTask jobTask = AnnotationUtil.getAnnotation(clazz, JobTask.class);
            if(ValidateUtils.isEmpty(jobTask)){
                logger.error("{}没有JobTask注解,不进行初始化",obj.getClass().getSimpleName());
                continue;
            }
            String className = obj.getClass().getSimpleName();
            String executorHandler = ValidateUtils.getOrDefault(jobTask.executorHandler(),className);

            XxlJobInfo xxlJobInfo = xxlJobInfoMap.get(executorHandler);

            boolean isCreate = ValidateUtils.isEmpty(xxlJobInfo);
            xxlJobInfo = isCreate ? new XxlJobInfo(jobTask,className) : xxlJobInfo.update(jobTask,className);
            xxlJobInfo.setExecutorHandler(executorHandler);
            // 发送 POST 请求
            if (isCreate) {
                service.saveJob(cookie, xxlJobInfo);
            } else {
                service.updateJob(cookie, xxlJobInfo);
            }
            //启用或禁止任务，调度类型必须不是不调度才可以
            if(!ValidateUtils.equalsIgnoreCase(xxlJobInfo.getScheduleType().name(), ScheduleTypeEnum.NONE.name())){
                if(isCreate){
                    if(jobTask.start()){
                        service.startJob(cookie, xxlJobInfo.getExecutorHandler());
                    } else {
                        service.stopJob(cookie, xxlJobInfo.getExecutorHandler());
                    }
                }
            } else {
                logger.info("{}类的调度类型为不调度,不允许启用或者禁止任务",className);
            }
            XxlJobSpringExecutor.registJobHandler(executorHandler, (IJobHandler) obj);
        }
    }

}
