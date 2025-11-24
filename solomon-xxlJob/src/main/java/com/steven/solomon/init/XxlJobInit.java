package com.steven.solomon.init;

import cn.hutool.core.annotation.AnnotationUtil;
import com.steven.solomon.annotation.JobTask;
import com.steven.solomon.entity.XxlJobInfo;
import com.steven.solomon.enums.ScheduleTypeEnum;
import com.steven.solomon.lambda.Lambda;
import com.steven.solomon.properties.XxlJobProperties;
import com.steven.solomon.service.CommonXxlJobService;
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

    private final CommonXxlJobService service;

    private final XxlJobSpringExecutor xxlJobSpringExecutor;

    public XxlJobInit(ApplicationContext applicationContext, XxlJobProperties profile, XxlJobSpringExecutor xxlJobSpringExecutor) {
        SpringUtil.setContext(applicationContext);
        this.profile = profile;
        Map<String,CommonXxlJobService> xxlJobServiceMap = SpringUtil.getBeansOfType(CommonXxlJobService.class);
        if(ValidateUtils.isEmpty(profile.getVersion())){
            service = xxlJobServiceMap.get(xxlJobServiceMap.keySet().stream()
                    .filter(s -> s.matches("^\\d+(\\.\\d+)*$")) // 匹配版本格式
                    .max((s1, s2) -> {
                        String[] parts1 = s1.split("\\.");
                        String[] parts2 = s2.split("\\.");

                        int maxLength = Math.max(parts1.length, parts2.length);

                        for (int i = 0; i < maxLength; i++) {
                            int num1 = (i < parts1.length) ? Integer.parseInt(parts1[i]) : 0;
                            int num2 = (i < parts2.length) ? Integer.parseInt(parts2[i]) : 0;

                            if (num1 != num2) {
                                return Integer.compare(num1, num2);
                            }
                        }
                        return 0;
                    })
                    .orElse(null));
        } else {
            this.service = xxlJobServiceMap.get(profile.getVersion());
        }
        this.xxlJobSpringExecutor = xxlJobSpringExecutor;

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
