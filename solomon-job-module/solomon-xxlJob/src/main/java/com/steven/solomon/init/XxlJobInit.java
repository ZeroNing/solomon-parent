package com.steven.solomon.init;

import cn.hutool.core.annotation.AnnotationUtil;
import com.steven.solomon.annotation.JobTask;
import com.steven.solomon.entity.XxlJobInfo;
import com.steven.solomon.enums.JobRegisterFailureStrategy;
import com.steven.solomon.enums.JobRegisterMode;
import com.steven.solomon.enums.JobPlatform;
import com.steven.solomon.enums.ScheduleTypeEnum;
import com.steven.solomon.config.XxlJobCondition;
import com.steven.solomon.properties.XxlJobProperties;
import com.steven.solomon.service.XxlJobService;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.verification.ValidateUtils;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.handler.IJobHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
@Import(XxlJobProperties.class)
@Conditional(XxlJobCondition.class)
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
        if (!profile.getEnabled()) {
            logger.error("xxl-Job不启用,不初始化定时任务");
            return;
        }
        if (!profile.getAutoRegister()) {
            logger.error("xxl-Job启用,但是配置了不允许自动注册");
            return;
        }
        String cookie = service.login();
        for (Object obj : clazzList) {
            Class<?> clazz = obj.getClass();
            JobTask jobTask = AnnotationUtil.getAnnotation(clazz, JobTask.class);
            if (ValidateUtils.isEmpty(jobTask)) {
                logger.error("{}没有JobTask注解,不进行初始化",obj.getClass().getSimpleName());
                continue;
            }
            if (!support(jobTask, obj)) {
                logger.info("{}未启用XXL-JOB平台,跳过自动注册", obj.getClass().getSimpleName());
                continue;
            }
            String className = obj.getClass().getSimpleName();
            String executorHandler = ValidateUtils.getOrDefault(jobTask.executorHandler(),className);
            int jobGroup = service.resolveJobGroup(cookie, jobTask.jobGroup());

            Map<String,XxlJobInfo> xxlJobInfoMap = service.findMapByExecutorHandler(cookie, executorHandler, jobGroup);
            XxlJobInfo xxlJobInfo = xxlJobInfoMap.get(executorHandler);

            register(cookie, jobTask, className, executorHandler, jobGroup, xxlJobInfo);
            XxlJobSpringExecutor.registJobHandler(executorHandler, (IJobHandler) obj);
        }
    }

    /**
     * 按配置的写入模式执行自动注册，避免启动时无条件覆盖线上任务。
     */
    private void register(String cookie, JobTask jobTask, String className, String executorHandler, int jobGroup, XxlJobInfo existsJob) throws Exception {
        try {
            boolean isCreate = ValidateUtils.isEmpty(existsJob);
            if (isCreate && JobRegisterMode.UPDATE_ONLY.equals(profile.getRegisterMode())) {
                logger.info("{}不存在，当前XXL-JOB注册模式为UPDATE_ONLY，跳过创建", executorHandler);
                return;
            }
            if (!isCreate && JobRegisterMode.CREATE_ONLY.equals(profile.getRegisterMode())) {
                logger.info("{}已存在，当前XXL-JOB注册模式为CREATE_ONLY，跳过更新", executorHandler);
                return;
            }
            XxlJobInfo xxlJobInfo = new XxlJobInfo(jobTask,className);
            xxlJobInfo.setJobGroup(jobGroup);
            xxlJobInfo.setExecutorHandler(executorHandler);
            if (isCreate) {
                service.saveJob(cookie, xxlJobInfo);
            } else {
                xxlJobInfo.setId(existsJob.getId());
                if (service.sameJob(existsJob, xxlJobInfo)) {
                    logger.info("{}任务配置未变化，跳过XXL-JOB更新", executorHandler);
                    syncStatus(cookie, jobTask, xxlJobInfo, false, className, jobGroup);
                    return;
                }
                service.updateJob(cookie, xxlJobInfo);
            }
            syncStatus(cookie, jobTask, xxlJobInfo, isCreate, className, jobGroup);
        } catch (Exception exception) {
            if (JobRegisterFailureStrategy.WARN_ONLY.equals(profile.getFailureStrategy())) {
                logger.warn("{}自动注册XXL-JOB失败，已按WARN_ONLY策略忽略", executorHandler, exception);
                return;
            }
            throw exception;
        }
    }

    /**
     * 创建任务后按注解同步启停状态，NONE 调度类型不执行启停。
     */
    private void syncStatus(String cookie, JobTask jobTask, XxlJobInfo xxlJobInfo, boolean isCreate, String className, int jobGroup) throws Exception {
        if (ValidateUtils.equalsIgnoreCase(xxlJobInfo.getScheduleType().name(), ScheduleTypeEnum.NONE.name())) {
            logger.info("{}类的调度类型为不调度,不允许启用或者禁止任务",className);
            return;
        }
        if (!isCreate && !profile.getSyncStatusOnUpdate()) {
            return;
        }
        if (jobTask.start()) {
            service.startJob(cookie, xxlJobInfo.getExecutorHandler(), jobGroup);
        } else {
            service.stopJob(cookie, xxlJobInfo.getExecutorHandler(), jobGroup);
        }
    }

    /**
     * 判断当前任务是否声明 XXL-JOB 平台。
     */
    private boolean support(JobTask jobTask, Object bean) {
        if (jobTask.platforms().length == 0) {
            return bean instanceof IJobHandler;
        }
        return Arrays.asList(jobTask.platforms()).contains(JobPlatform.XXL_JOB);
    }

}
