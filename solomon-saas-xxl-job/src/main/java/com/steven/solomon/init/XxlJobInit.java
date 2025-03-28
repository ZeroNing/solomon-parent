package com.steven.solomon.init;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.annotation.JobTask;
import com.steven.solomon.consumer.AbstractJobConsumer;
import com.steven.solomon.entity.XxlJobInfo;
import com.steven.solomon.enums.ScheduleTypeEnum;
import com.steven.solomon.lambda.Lambda;
import com.steven.solomon.properties.SaasXxlJobProperties;
import com.steven.solomon.properties.XxlJobProperties;
import com.steven.solomon.service.XxlJobService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.JobUtils;
import com.steven.solomon.verification.ValidateUtils;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.handler.IJobHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Map;

@Configuration
@Import({SaasXxlJobProperties.class,JobUtils.class})
public class XxlJobInit extends AbstractMessageLineRunner<JobTask> {

    private final SaasXxlJobProperties profile;

    private final XxlJobService service;

    private final JobUtils utils;

    public XxlJobInit(ApplicationContext applicationContext, SaasXxlJobProperties profile, XxlJobService service, JobUtils utils) {
        this.profile = profile;
        this.service = service;
        this.utils = utils;
        SpringUtil.setContext(applicationContext);
    }

    @Override
    public void init(List<Object> clazzList) throws Exception {
        if(!profile.getEnabled()){
            logger.error("xxl-Job不启用,不初始化定时任务");
            return;
        }
        Map<String,XxlJobProperties> tenantPropertiesMap = profile.getTenant();
        if(ValidateUtils.isEmpty(tenantPropertiesMap)){
            logger.error("多租户xxl-Job配置为空,不初始化定时任务");
            return;
        }
        for(Map.Entry<String,XxlJobProperties> key : tenantPropertiesMap.entrySet()){
            String tenantCode = key.getKey();
            XxlJobProperties properties = key.getValue();
            XxlJobSpringExecutor springExecutor = xxlJobExecutor(properties);

            String cookie = service.login(properties);
            for(Object obj : clazzList){
                Class<?> clazz = obj.getClass();
                JobTask jobTask = AnnotationUtil.getAnnotation(clazz, JobTask.class);
                if(ValidateUtils.isEmpty(jobTask)){
                    logger.error("租户:{},{}没有JobTask注解,不进行初始化",tenantCode,obj.getClass().getSimpleName());
                    continue;
                }
                if(StrUtil.isNotBlank(jobTask.tenantCode()) && !StrUtil.equalsIgnoreCase(jobTask.tenantCode(),tenantCode)){
                    logger.error("租户:{},不允许创建当前任务:{},因为非这个{}租户的任务",tenantCode,obj.getClass().getSimpleName(),jobTask.tenantCode());
                    continue;
                }
                String className = obj.getClass().getSimpleName();
                String executorHandler = ValidateUtils.getOrDefault(jobTask.executorHandler(),className);

                List<XxlJobInfo> xxlJobInfoList = service.findByExecutorHandler(cookie,executorHandler,properties);
                Map<String,XxlJobInfo> xxlJobInfoMap = Lambda.toMap(xxlJobInfoList, XxlJobInfo::getExecutorHandler);
                XxlJobInfo xxlJobInfo = xxlJobInfoMap.get(executorHandler);

                boolean isCreate = ValidateUtils.isEmpty(xxlJobInfo);
                xxlJobInfo = isCreate ? new XxlJobInfo(jobTask,className) : xxlJobInfo.update(jobTask,className);
                xxlJobInfo.setExecutorHandler(executorHandler);
                // 发送 POST 请求
                if (isCreate) {
                    service.saveJob(cookie, xxlJobInfo,properties);
                } else {
                    service.updateJob(cookie, xxlJobInfo,properties);
                }
                //启用或禁止任务，调度类型必须不是不调度才可以
                if(!ValidateUtils.equalsIgnoreCase(xxlJobInfo.getScheduleType().name(), ScheduleTypeEnum.NONE.name())){
                    if(isCreate){
                        if(jobTask.start()){
                            service.startJob(cookie, xxlJobInfo.getExecutorHandler(),properties);
                        } else {
                            service.stopJob(cookie, xxlJobInfo.getExecutorHandler(),properties);
                        }
                    }
                } else {
                    logger.info("租户:{},{}类的调度类型为不调度,不允许启用或者禁止任务",tenantCode,className);
                }
                AbstractJobConsumer consumer = (AbstractJobConsumer) BeanUtil.copyProperties(obj,obj.getClass(), (String) null);
                consumer.setTenantCode(tenantCode);
                springExecutor.registJobHandler(executorHandler, consumer);
                utils.putJobExecutorMap(tenantCode,springExecutor);
            }
        }
    }

    public XxlJobSpringExecutor xxlJobExecutor(XxlJobProperties profile) throws Exception {
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(profile.getAdminAddresses());
        xxlJobSpringExecutor.setAppname(profile.getAppName());
        xxlJobSpringExecutor.setIp(profile.getIp());
        xxlJobSpringExecutor.setPort(profile.getPort());
        xxlJobSpringExecutor.setAccessToken(profile.getAccessToken());
        if(ValidateUtils.isNotEmpty(profile.getLogPath())){
            xxlJobSpringExecutor.setLogPath(profile.getLogPath());
        }
        xxlJobSpringExecutor.setLogRetentionDays(profile.getLogRetentionDays());
        return xxlJobSpringExecutor;
    }
}
