package com.steven.solomon.enums;


/**
 * 任务日志配置实体。
 *
 * <p>对应 PowerJob 任务的日志输出配置，包含日志类型、级别和 Logger 名称。
 * 由 {@link tech.powerjob.common.model.LogConfig} 序列化而来。</p>
 *
 * @author yhz
 * @since 2022/9/16
 */
public class LogConfig {

    /**
     * 日志类型，对应 {@link tech.powerjob.common.enums.LogType}
     */
    private Integer type;

    /**
     * 日志级别，对应 {@link tech.powerjob.common.enums.LogLevel}
     */
    private Integer level;

    /** Logger 名称，用于标识日志输出源。 */
    private String loggerName;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }
}
