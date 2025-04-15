package com.steven.solomon.entity;

import cn.hutool.core.date.DateUtil;
import com.steven.solomon.annotation.JobTask;
import com.steven.solomon.enums.*;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.date.DateTimeUtils;
import com.steven.solomon.verification.ValidateUtils;
import tech.powerjob.common.model.AlarmConfig;
import tech.powerjob.common.model.LifeCycle;
import tech.powerjob.common.response.JobInfoDTO;
import tech.powerjob.common.utils.CommonUtils;

import java.util.List;

/**
 * SaveJobInfoRequest 类用于封装保存或更新作业信息的请求数据。
 */
public class SaveJobInfoRequest {

    /**
     * 作业的唯一标识符。如果为 null，则创建新作业；如果不为 null，则更新现有作业。
     */
    private Long id;

    /* ************************** 作业的基本信息 ************************** */

    /**
     * 作业名称。
     */
    private String jobName;

    /**
     * 作业描述。
     */
    private String jobDescription;

    /**
     * 应用程序的相关 ID。在 PowerJob-client 中不需要设置此属性，它会自动设置。
     */
    private Long appId;

    /**
     * 创建作业时携带的参数。
     */
    private String jobParams;

    /* ************************** 定时参数 ************************** */

    /**
     * 时间表达式类型。
     */
    private TimeExpressionType timeExpressionType;

    /**
     * 时间表达式。
     */
    private String timeExpression;

    /* ************************** 执行类型 ************************** */

    /**
     * 执行类型，例如 {@code standalone}、{@code broadcast} 或 {@code Map/MapReduce}。
     */
    private ExecuteType executeType;

    /**
     * 处理器类型，例如 {@code Java}、{@code Python} 或 {@code Shell}。
     */
    private ProcessorType processorType;

    /**
     * 处理器信息。
     */
    private String processorInfo;

    /* ************************** 运行实例设置 ************************** */

    /**
     * 最大实例数设置。{@code 0}表示没有限制。
     */
    private Integer maxInstanceNum = 0;

    /**
     * 并发设置。表示同时运行的线程数。
     */
    private Integer concurrency = 5;

    /**
     * 实例运行时间限制。{@code 0L}表示没有限制。
     */
    private Long instanceTimeLimit = 0L;

    /* ************************** 重试设置 ************************** */

    /**
     * 实例重试次数设置。
     */
    private Integer instanceRetryNum = 0;

    /**
     * 任务重试次数设置。
     */
    private Integer taskRetryNum = 0;

    /* ************************** 忙碌机器设置 ************************** */

    /**
     * 最小 CPU 要求。{@code 0}表示没有限制。
     */
    private double minCpuCores = 0;

    /**
     * 最小内存要求，以 GB 为单位。
     */
    private double minMemorySpace = 0;

    /**
     * 最小磁盘空间，以 GB 为单位。{@code 0}表示没有限制。
     */
    private double minDiskSpace = 0;

    /**
     * 是否启用作业。
     */
    private boolean enable = true;

    /* ************************** PowerJob-worker 集群属性 ************************** */

    /**
     * 指定的 PowerJob-worker 节点。空白值表示没有限制，非空值表示仅在对应机器上运行。
     * 例如：192.168.1.1:27777,192.168.1.2:27777。
     */
    private String designatedWorkers;

    /**
     * PowerJob-worker 节点的最大数量。
     */
    private Integer maxWorkerCount = 0;

    /**
     * 需要通知的用户 ID 列表。
     */
    private List<Long> notifyUserIds;

    /**
     * 额外的配置信息。
     */
    private String extra;

    /**
     * 派发策略。
     */
    private DispatchStrategy dispatchStrategy;

    /**
     * 某种派发策略背后的具体配置，值取决于 dispatchStrategy。
     */
    private String dispatchStrategyConfig;

    /**
     * 生命周期配置。
     */
    private LifeCycle lifeCycle;

    /* ************************** 告警配置 ************************** */

    /**
     * 告警配置。
     */
    private AlarmConfig alarmConfig;

    /**
     * 任务归类，开放给接入方自由定制。
     */
    private String tag;

    /**
     * 日志配置，包括日志级别、日志方式等配置信息。
     */
    private LogConfig logConfig;

    /**
     * 高级运行时配置。
     */
    private JobAdvancedRuntimeConfig advancedRuntimeConfig;

    public SaveJobInfoRequest() {
        super();
    }

    public SaveJobInfoRequest update(JobTask jobTask,String className){
        this.jobName = SpringUtil.getElValue(jobTask.taskName());
        this.jobDescription = jobTask.taskDesc();
        this.appId = Long.valueOf(appId);
        this.jobParams = jobTask.taskParams();
        this.timeExpressionType = jobTask.timeExpressionType();
        this.timeExpression = jobTask.timeExpression();
        this.executeType = jobTask.executeType();
        this.processorType = jobTask.processorType();
        this.processorInfo = ValidateUtils.getOrDefault(jobTask.processorInfo(),className);
        this.maxInstanceNum = jobTask.maxInstanceNum();
        this.concurrency = jobTask.concurrency();
        this.instanceTimeLimit = jobTask.instanceTimeLimit();
        this.instanceRetryNum = jobTask.instanceRetryNum();
        this.taskRetryNum = jobTask.taskRetryNum();
        this.minCpuCores = jobTask.minCpuCores();
        this.minMemorySpace = jobTask.minMemorySpace();
        this.minDiskSpace = jobTask.minDiskSpace();
        this.enable = jobTask.enable();
        this.dispatchStrategy = jobTask.dispatchStrategy();
        this.dispatchStrategyConfig = jobTask.dispatchStrategyConfig();
        if(ValidateUtils.isNotEmpty(jobTask.lifeCycleStart()) && ValidateUtils.isNotEmpty(jobTask.lifeCycleEnd())){
            LifeCycle life = new LifeCycle();
            life.setStart(DateUtil.parse(jobTask.lifeCycleStart(),"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd HH:mm:ss.SSS","yyyy-MM-dd HH:mm:ss:SSS").getTime());
            life.setEnd(DateUtil.parse(jobTask.lifeCycleEnd(),"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd HH:mm:ss.SSS","yyyy-MM-dd HH:mm:ss:SSS").getTime());
            this.lifeCycle = life;
        }
        AlarmConfig alarmConfig = new AlarmConfig();
        if(!ValidateUtils.equals(-1,jobTask.alertThreshold())){
            alarmConfig.setAlertThreshold(jobTask.alertThreshold());
            this.alarmConfig = alarmConfig;
        }
        if(!ValidateUtils.equals(-1,jobTask.statisticWindowLen())){
            alarmConfig.setStatisticWindowLen(jobTask.statisticWindowLen());
            this.alarmConfig = alarmConfig;
        }
        if(!ValidateUtils.equals(-1,jobTask.silenceWindowLen())){
            alarmConfig.setSilenceWindowLen(jobTask.silenceWindowLen());
            this.alarmConfig = alarmConfig;
        }
        this.logConfig = new LogConfig();
        if(ValidateUtils.isNotEmpty(jobTask.type())){
            logConfig.setType(jobTask.type().label());
        }
        if(ValidateUtils.isNotEmpty(jobTask.level())){
            logConfig.setLevel(jobTask.level().label());
        }
        this.advancedRuntimeConfig = new JobAdvancedRuntimeConfig();
        if(ValidateUtils.isNotEmpty(jobTask.taskTrackerBehavior())){
            this.advancedRuntimeConfig.setTaskTrackerBehavior(jobTask.taskTrackerBehavior().label());
        }
        return this;
    }

    public SaveJobInfoRequest(JobTask jobTask,Integer appId,String className) {
        super();
        this.jobName = SpringUtil.getElValue(jobTask.taskName());
        this.jobDescription = jobTask.taskDesc();
        this.appId = Long.valueOf(appId);
        this.jobParams = jobTask.taskParams();
        this.timeExpressionType = jobTask.timeExpressionType();
        this.timeExpression = jobTask.timeExpression();
        this.executeType = jobTask.executeType();
        this.processorType = jobTask.processorType();
        this.processorInfo = ValidateUtils.getOrDefault(jobTask.processorInfo(),className);
        this.maxInstanceNum = jobTask.maxInstanceNum();
        this.concurrency = jobTask.concurrency();
        this.instanceTimeLimit = jobTask.instanceTimeLimit();
        this.instanceRetryNum = jobTask.instanceRetryNum();
        this.taskRetryNum = jobTask.taskRetryNum();
        this.minCpuCores = jobTask.minCpuCores();
        this.minMemorySpace = jobTask.minMemorySpace();
        this.minDiskSpace = jobTask.minDiskSpace();
        this.enable = jobTask.enable();
        this.dispatchStrategy = jobTask.dispatchStrategy();
        this.dispatchStrategyConfig = jobTask.dispatchStrategyConfig();
        if(ValidateUtils.isNotEmpty(jobTask.lifeCycleStart()) && ValidateUtils.isNotEmpty(jobTask.lifeCycleEnd())){
            LifeCycle life = new LifeCycle();
            life.setStart(DateUtil.parse(jobTask.lifeCycleStart(),"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd HH:mm:ss.SSS","yyyy-MM-dd HH:mm:ss:SSS").getTime());
            life.setEnd(DateUtil.parse(jobTask.lifeCycleEnd(),"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd HH:mm:ss.SSS","yyyy-MM-dd HH:mm:ss:SSS").getTime());
            this.lifeCycle = life;
        }
        AlarmConfig alarmConfig = new AlarmConfig();
        alarmConfig.setAlertThreshold(jobTask.alertThreshold());
        alarmConfig.setStatisticWindowLen(jobTask.statisticWindowLen());
        alarmConfig.setSilenceWindowLen(jobTask.silenceWindowLen());
        this.alarmConfig = alarmConfig;
        this.logConfig = new LogConfig();
        if(ValidateUtils.isNotEmpty(jobTask.type())){
            logConfig.setType(jobTask.type().label());
        }
        if(ValidateUtils.isNotEmpty(jobTask.level())){
            logConfig.setLevel(jobTask.level().label());
        }
        this.advancedRuntimeConfig = new JobAdvancedRuntimeConfig();
        if(ValidateUtils.isNotEmpty(jobTask.taskTrackerBehavior())){
            this.advancedRuntimeConfig.setTaskTrackerBehavior(jobTask.taskTrackerBehavior().label());
        }
    }

    // 以下为 getter 和 setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getJobParams() {
        return jobParams;
    }

    public void setJobParams(String jobParams) {
        this.jobParams = jobParams;
    }

    public TimeExpressionType getTimeExpressionType() {
        return timeExpressionType;
    }

    public void setTimeExpressionType(TimeExpressionType timeExpressionType) {
        this.timeExpressionType = timeExpressionType;
    }

    public String getTimeExpression() {
        return timeExpression;
    }

    public void setTimeExpression(String timeExpression) {
        this.timeExpression = timeExpression;
    }

    public ExecuteType getExecuteType() {
        return executeType;
    }

    public void setExecuteType(ExecuteType executeType) {
        this.executeType = executeType;
    }

    public ProcessorType getProcessorType() {
        return processorType;
    }

    public void setProcessorType(ProcessorType processorType) {
        this.processorType = processorType;
    }

    public String getProcessorInfo() {
        return processorInfo;
    }

    public void setProcessorInfo(String processorInfo) {
        this.processorInfo = processorInfo;
    }

    public Integer getMaxInstanceNum() {
        return maxInstanceNum;
    }

    public void setMaxInstanceNum(Integer maxInstanceNum) {
        this.maxInstanceNum = maxInstanceNum;
    }

    public Integer getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(Integer concurrency) {
        this.concurrency = concurrency;
    }

    public Long getInstanceTimeLimit() {
        return instanceTimeLimit;
    }

    public void setInstanceTimeLimit(Long instanceTimeLimit) {
        this.instanceTimeLimit = instanceTimeLimit;
    }

    public Integer getInstanceRetryNum() {
        return instanceRetryNum;
    }

    public void setInstanceRetryNum(Integer instanceRetryNum) {
        this.instanceRetryNum = instanceRetryNum;
    }

    public Integer getTaskRetryNum() {
        return taskRetryNum;
    }

    public void setTaskRetryNum(Integer taskRetryNum) {
        this.taskRetryNum = taskRetryNum;
    }

    public double getMinCpuCores() {
        return minCpuCores;
    }

    public void setMinCpuCores(double minCpuCores) {
        this.minCpuCores = minCpuCores;
    }

    public double getMinMemorySpace() {
        return minMemorySpace;
    }

    public void setMinMemorySpace(double minMemorySpace) {
        this.minMemorySpace = minMemorySpace;
    }

    public double getMinDiskSpace() {
        return minDiskSpace;
    }

    public void setMinDiskSpace(double minDiskSpace) {
        this.minDiskSpace = minDiskSpace;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getDesignatedWorkers() {
        return designatedWorkers;
    }

    public void setDesignatedWorkers(String designatedWorkers) {
        this.designatedWorkers = designatedWorkers;
    }

    public Integer getMaxWorkerCount() {
        return maxWorkerCount;
    }

    public void setMaxWorkerCount(Integer maxWorkerCount) {
        this.maxWorkerCount = maxWorkerCount;
    }

    public List<Long> getNotifyUserIds() {
        return notifyUserIds;
    }

    public void setNotifyUserIds(List<Long> notifyUserIds) {
        this.notifyUserIds = notifyUserIds;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public DispatchStrategy getDispatchStrategy() {
        return dispatchStrategy;
    }

    public void setDispatchStrategy(DispatchStrategy dispatchStrategy) {
        this.dispatchStrategy = dispatchStrategy;
    }

    public String getDispatchStrategyConfig() {
        return dispatchStrategyConfig;
    }

    public void setDispatchStrategyConfig(String dispatchStrategyConfig) {
        this.dispatchStrategyConfig = dispatchStrategyConfig;
    }

    public LifeCycle getLifeCycle() {
        return lifeCycle;
    }

    public void setLifeCycle(LifeCycle lifeCycle) {
        this.lifeCycle = lifeCycle;
    }

    public AlarmConfig getAlarmConfig() {
        return alarmConfig;
    }

    public void setAlarmConfig(AlarmConfig alarmConfig) {
        this.alarmConfig = alarmConfig;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public LogConfig getLogConfig() {
        return logConfig;
    }

    public void setLogConfig(LogConfig logConfig) {
        this.logConfig = logConfig;
    }

    public JobAdvancedRuntimeConfig getAdvancedRuntimeConfig() {
        return advancedRuntimeConfig;
    }

    public void setAdvancedRuntimeConfig(JobAdvancedRuntimeConfig advancedRuntimeConfig) {
        this.advancedRuntimeConfig = advancedRuntimeConfig;
    }
}

