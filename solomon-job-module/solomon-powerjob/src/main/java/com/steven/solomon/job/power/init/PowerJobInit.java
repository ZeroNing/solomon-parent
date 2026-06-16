package com.steven.solomon.job.power.init;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.core.annotation.AnnotationUtil;
import com.steven.solomon.annotation.JobTask;
import com.steven.solomon.job.power.config.PowerJobCondition;
import com.steven.solomon.job.power.entity.PowerJobRequestFactory;
import com.steven.solomon.enums.JobRegisterFailureStrategy;
import com.steven.solomon.enums.JobRegisterMode;
import com.steven.solomon.enums.JobPlatform;
import com.steven.solomon.job.power.properties.PowerJobRegisterProperties;
import com.steven.solomon.job.power.service.PowerJobService;
import com.steven.solomon.spring.SpringUtil;
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

/**
 * PowerJob 任务自动注册初始化器。
 *
 * <p>在应用启动时，扫描所有标注 {@link JobTask} 注解且支持 PowerJob 平台的 Bean，
 * 自动登录管理端、创建命名空间和应用，并按照 {@link JobRegisterMode} 配置执行任务注册（新增/更新）。
 * 支持通过 {@link JobRegisterFailureStrategy} 配置注册失败时的行为。</p>
 */
@Configuration
@Import(value = {PowerJobRegisterProperties.class})
@Conditional(PowerJobCondition.class)
@Order(1)
public class PowerJobInit extends AbstractMessageLineRunner<JobTask> {

    /** PowerJob Worker 配置，用于获取应用名称和连接信息。 */
    private final PowerJobProperties powerJobProperties;

    /** PowerJob 管理端 API 服务。 */
    private final PowerJobService service;

    /** 自动注册配置。 */
    private final PowerJobRegisterProperties registerProperties;

    public PowerJobInit(ApplicationContext applicationContext, PowerJobProperties powerJobProperties, PowerJobService service, PowerJobRegisterProperties registerProperties) {
        this.powerJobProperties = powerJobProperties;
        this.service = service;
        SpringUtil.setContext(applicationContext);
        this.registerProperties = registerProperties;
    }

    @Override
    public void init(List<Object> clazzList) throws Exception {
        if (!powerJobProperties.getWorker().isEnabled()) {
            logger.error("powerJob不启用,不初始化定时任务");
            return;
        }
        if (!registerProperties.getEnabled()) {
            logger.error("powerJob启用,配置了不自动注册任务");
            return;
        }
        //登陆获取cookie
        String cookie = service.login();
        //创建命名空间
        Integer namespaceId = service.createNamespace(registerProperties.getNamespace(),cookie);
        //根据appName创建namespace并返回namespaceId
        Integer appId = service.createAppId(cookie,powerJobProperties.getWorker().getAppName(),namespaceId);
        //获取全部任务
        Map<String,SaveJobInfoRequest> taskMap = service.findByExecutorHandler(cookie,appId);
        for (Object obj : clazzList) {
            Class<?> clazz = AopUtils.getTargetClass(obj);
            JobTask jobTask = AnnotationUtil.getAnnotation(clazz, JobTask.class);
            if (ObjectUtil.isEmpty(jobTask)) {
                logger.error("{}没有JobTask注解,不进行初始化",clazz.getSimpleName());
                continue;
            }
            if (!support(jobTask, clazz)) {
                logger.info("{}未启用PowerJob平台,跳过自动注册", clazz.getSimpleName());
                continue;
            }
            String className = clazz.getName();
            register(cookie, taskMap, jobTask, appId, className);
        }
    }

    /**
     * 按配置的写入模式执行自动注册，避免启动时无条件覆盖线上任务。
     */
    private void register(String cookie, Map<String, SaveJobInfoRequest> taskMap, JobTask jobTask, Integer appId, String className) throws Exception {
        try {
            SaveJobInfoRequest saveRequest = taskMap.get(className);
            if (ObjectUtil.isEmpty(saveRequest)) {
                if (JobRegisterMode.UPDATE_ONLY.equals(registerProperties.getMode())) {
                    logger.info("{}不存在，当前PowerJob注册模式为UPDATE_ONLY，跳过创建", className);
                    return;
                }
                service.saveJob(cookie, PowerJobRequestFactory.create(jobTask, appId, className));
                return;
            }
            if (JobRegisterMode.CREATE_ONLY.equals(registerProperties.getMode())) {
                logger.info("{}已存在，当前PowerJob注册模式为CREATE_ONLY，跳过更新", className);
                return;
            }
            if (PowerJobRequestFactory.samePayload(saveRequest, jobTask, className)) {
                logger.info("{}任务配置未变化，跳过PowerJob更新", className);
                return;
            }
            service.updateJob(cookie, PowerJobRequestFactory.update(saveRequest, jobTask, className));
        } catch (Exception exception) {
            if (JobRegisterFailureStrategy.WARN_ONLY.equals(registerProperties.getFailureStrategy())) {
                logger.warn("{}自动注册PowerJob失败，已按WARN_ONLY策略忽略", className, exception);
                return;
            }
            throw exception;
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
