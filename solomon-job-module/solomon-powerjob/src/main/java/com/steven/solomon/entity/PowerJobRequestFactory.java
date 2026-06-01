package com.steven.solomon.entity;

import cn.hutool.core.util.ObjectUtil;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.steven.solomon.annotation.JobTask;
import com.steven.solomon.spring.SpringUtil;
import tech.powerjob.common.model.AlarmConfig;
import tech.powerjob.common.model.JobAdvancedRuntimeConfig;
import tech.powerjob.common.model.LifeCycle;
import tech.powerjob.common.model.LogConfig;
import tech.powerjob.common.request.http.SaveJobInfoRequest;
import tech.powerjob.common.response.JobInfoDTO;

import java.util.Map;

/**
 * PowerJob 请求转换器。
 * <p>
 * 保存、更新模型直接使用 PowerJob 官方 jar 内的 {@link SaveJobInfoRequest}，
 * 本类只负责把 Solomon 的统一注解转换成官方请求，并补齐历史版本接口字段。
 */
public final class PowerJobRequestFactory {

    private PowerJobRequestFactory() {
    }

    /**
     * 创建新增任务请求。
     */
    public static SaveJobInfoRequest create(JobTask jobTask, Integer appId, String className) {
        SaveJobInfoRequest request = new SaveJobInfoRequest();
        request.setAppId(Long.valueOf(appId));
        return fill(request, jobTask, className);
    }

    /**
     * 基于已有任务创建更新请求，保留任务 ID 与应用 ID。
     */
    public static SaveJobInfoRequest update(SaveJobInfoRequest request, JobTask jobTask, String className) {
        return fill(request, jobTask, className);
    }

    /**
     * 判断注解生成的任务参数与管理端已有任务是否一致。
     */
    public static boolean samePayload(SaveJobInfoRequest existsRequest, JobTask jobTask, String className) {
        SaveJobInfoRequest target = copyIdentity(existsRequest);
        fill(target, jobTask, className);
        return toPayload(existsRequest).equals(toPayload(target));
    }

    /**
     * 将管理端查询结果转换成官方保存请求，方便复用同一套更新逻辑。
     */
    public static SaveJobInfoRequest from(JobInfoDTO jobInfo) {
        SaveJobInfoRequest request = new SaveJobInfoRequest();
        request.setId(jobInfo.getId());
        request.setJobName(jobInfo.getJobName());
        request.setJobDescription(jobInfo.getJobDescription());
        request.setAppId(jobInfo.getAppId());
        request.setJobParams(jobInfo.getJobParams());
        request.setTimeExpressionType(tech.powerjob.common.enums.TimeExpressionType.of(jobInfo.getTimeExpressionType()));
        request.setTimeExpression(jobInfo.getTimeExpression());
        request.setExecuteType(tech.powerjob.common.enums.ExecuteType.of(jobInfo.getExecuteType()));
        request.setProcessorType(tech.powerjob.common.enums.ProcessorType.of(jobInfo.getProcessorType()));
        request.setProcessorInfo(jobInfo.getProcessorInfo());
        request.setMaxInstanceNum(jobInfo.getMaxInstanceNum());
        request.setConcurrency(jobInfo.getConcurrency());
        request.setInstanceTimeLimit(jobInfo.getInstanceTimeLimit());
        request.setInstanceRetryNum(jobInfo.getInstanceRetryNum());
        request.setTaskRetryNum(jobInfo.getTaskRetryNum());
        request.setMinCpuCores(jobInfo.getMinCpuCores());
        request.setMinMemorySpace(jobInfo.getMinMemorySpace());
        request.setMinDiskSpace(jobInfo.getMinDiskSpace());
        request.setEnable(!ObjectUtil.equals(jobInfo.getStatus(), 2));
        request.setDesignatedWorkers(jobInfo.getDesignatedWorkers());
        request.setMaxWorkerCount(jobInfo.getMaxWorkerCount());
        request.setExtra(jobInfo.getExtra());
        request.setDispatchStrategy(tech.powerjob.common.enums.DispatchStrategy.of(jobInfo.getDispatchStrategy()));
        request.setDispatchStrategyConfig(jobInfo.getDispatchStrategyConfig());
        request.setAlarmConfig(jobInfo.getAlarmConfig());
        request.setTag(jobInfo.getTag());
        request.setLogConfig(jobInfo.getLogConfig());
        request.setAdvancedRuntimeConfig(jobInfo.getAdvancedRuntimeConfig());
        return request;
    }

    /**
     * 生成最终请求体，同时携带 5.x 的 lifeCycle 和 4.x 的 lifecycle 字段。
     */
    public static Map<String, Object> toPayload(SaveJobInfoRequest request) {
        Map<String, Object> payload = JSONUtil.toBean(JSONUtil.toJsonStr(request), new TypeReference<Map<String, Object>>() {}, true);
        if (ObjectUtil.isNotEmpty(request.getLifeCycle())) {
            payload.put("lifecycle", JSONUtil.toJsonStr(request.getLifeCycle()));
        }
        return payload;
    }

    /**
     * 仅复制任务身份字段，用于构造待比较的新请求。
     */
    private static SaveJobInfoRequest copyIdentity(SaveJobInfoRequest source) {
        SaveJobInfoRequest target = new SaveJobInfoRequest();
        target.setId(source.getId());
        target.setAppId(source.getAppId());
        return target;
    }

    /**
     * 按注解统一填充 PowerJob 任务参数，保证创建和更新字段完全一致。
     */
    private static SaveJobInfoRequest fill(SaveJobInfoRequest request, JobTask jobTask, String className) {
        request.setJobName(SpringUtil.getElValue(ObjectUtil.defaultIfNull(jobTask.taskName(), className)));
        request.setJobDescription(jobTask.taskDesc());
        request.setJobParams(jobTask.taskParams());
        request.setTimeExpressionType(tech.powerjob.common.enums.TimeExpressionType.valueOf(jobTask.timeExpressionType().name()));
        request.setTimeExpression(jobTask.timeExpression());
        request.setExecuteType(tech.powerjob.common.enums.ExecuteType.valueOf(jobTask.executeType().name()));
        request.setProcessorType(tech.powerjob.common.enums.ProcessorType.valueOf(jobTask.processorType().name()));
        request.setProcessorInfo(ObjectUtil.defaultIfNull(jobTask.processorInfo(), className));
        request.setMaxInstanceNum(jobTask.maxInstanceNum());
        request.setConcurrency(jobTask.concurrency());
        request.setInstanceTimeLimit(jobTask.instanceTimeLimit());
        request.setInstanceRetryNum(jobTask.instanceRetryNum());
        request.setTaskRetryNum(jobTask.taskRetryNum());
        request.setMinCpuCores(jobTask.minCpuCores());
        request.setMinMemorySpace(jobTask.minMemorySpace());
        request.setMinDiskSpace(jobTask.minDiskSpace());
        request.setEnable(jobTask.enable());
        request.setDispatchStrategy(tech.powerjob.common.enums.DispatchStrategy.valueOf(jobTask.dispatchStrategy().name()));
        request.setDispatchStrategyConfig(jobTask.dispatchStrategyConfig());
        request.setLifeCycle(buildLifeCycle(jobTask));
        request.setAlarmConfig(buildAlarmConfig(jobTask));
        request.setLogConfig(new LogConfig()
                .setType(jobTask.type().label())
                .setLevel(jobTask.level().label()));
        request.setAdvancedRuntimeConfig(new JobAdvancedRuntimeConfig()
                .setTaskTrackerBehavior(jobTask.taskTrackerBehavior().label()));
        return request;
    }

    /**
     * PowerJob 5.x 使用结构化生命周期配置。
     */
    private static LifeCycle buildLifeCycle(JobTask jobTask) {
        if (ObjectUtil.isEmpty(jobTask.lifeCycleStart()) || ObjectUtil.isEmpty(jobTask.lifeCycleEnd())) {
            return null;
        }
        LifeCycle lifeCycle = new LifeCycle();
        lifeCycle.setStart(DateUtil.parse(jobTask.lifeCycleStart(), "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss:SSS").getTime());
        lifeCycle.setEnd(DateUtil.parse(jobTask.lifeCycleEnd(), "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss:SSS").getTime());
        return lifeCycle;
    }

    /**
     * 告警配置统一封装，未配置时保持 PowerJob 默认值。
     */
    private static AlarmConfig buildAlarmConfig(JobTask jobTask) {
        AlarmConfig alarmConfig = new AlarmConfig();
        alarmConfig.setAlertThreshold(jobTask.alertThreshold());
        alarmConfig.setStatisticWindowLen(jobTask.statisticWindowLen());
        alarmConfig.setSilenceWindowLen(jobTask.silenceWindowLen());
        return alarmConfig;
    }
}
