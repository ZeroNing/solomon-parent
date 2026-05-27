package com.steven.solomon.init;

import cn.hutool.core.annotation.AnnotationUtil;
import com.steven.solomon.annotation.JobTask;
import com.steven.solomon.config.PowerJobCondition;
import com.steven.solomon.entity.PowerJobRequestFactory;
import com.steven.solomon.enums.JobPlatform;
import com.steven.solomon.properties.JobProperties;
import com.steven.solomon.service.PowerJobService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.verification.ValidateUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import tech.powerjob.common.request.http.SaveJobInfoRequest;
import tech.powerjob.worker.autoconfigure.PowerJobProperties;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
@Import(value = {JobProperties.class})
@Conditional(PowerJobCondition.class)
@Order(1)
public class PowerJobInit extends AbstractMessageLineRunner<JobTask> {

    private final PowerJobProperties powerJobProperties;

    private final PowerJobService service;
    private final JobProperties jobProperties;

    public PowerJobInit(ApplicationContext applicationContext, PowerJobProperties powerJobProperties, PowerJobService service, JobProperties jobProperties) {
        this.powerJobProperties = powerJobProperties;
        this.service = service;
        SpringUtil.setContext(applicationContext);
        this.jobProperties = jobProperties;
    }

    @Override
    public void init(List<Object> clazzList) throws Exception {
        if (!powerJobProperties.getWorker().isEnabled()) {
            logger.error("powerJob不启用,不初始化定时任务");
            return;
        }
        if (!jobProperties.getAutoRegister()) {
            logger.error("powerJob启用,配置了不自动注册任务");
            return;
        }
        //登陆获取cookie
        String cookie = service.login();
        //创建命名空间
        Integer namespaceId = service.createNamespace(jobProperties.getNamespace(),cookie);
        //根据appName创建namespace并返回namespaceId
        Integer appId = service.createAppId(cookie,powerJobProperties.getWorker().getAppName(),namespaceId);
        //获取全部任务
        Map<String,SaveJobInfoRequest> taskMap = service.findByExecutorHandler(cookie,appId);
        for (Object obj : clazzList) {
            Class<?> clazz = AopUtils.getTargetClass(obj);
            JobTask jobTask = AnnotationUtil.getAnnotation(clazz, JobTask.class);
            if (ValidateUtils.isEmpty(jobTask)) {
                logger.error("{}没有JobTask注解,不进行初始化",clazz.getSimpleName());
                continue;
            }
            if (!support(jobTask, clazz)) {
                logger.info("{}未启用PowerJob平台,跳过自动注册", clazz.getSimpleName());
                continue;
            }
            String className = clazz.getName();
            SaveJobInfoRequest saveRequest = taskMap.get(className);
            if (ValidateUtils.isEmpty(saveRequest)) {
                saveRequest = PowerJobRequestFactory.create(jobTask,appId,className);
                service.saveJob(cookie,saveRequest);
            } else {
                saveRequest = PowerJobRequestFactory.update(saveRequest,jobTask,className);
                service.updateJob(cookie,saveRequest);
            }
        }
    }

    /**
     * 判断当前任务是否声明 PowerJob 平台。
     */
    private boolean support(JobTask jobTask, Class<?> clazz) {
        if (jobTask.platforms().length == 0) {
            return BasicProcessor.class.isAssignableFrom(clazz);
        }
        return Arrays.asList(jobTask.platforms()).contains(JobPlatform.POWERJOB);
    }

}
