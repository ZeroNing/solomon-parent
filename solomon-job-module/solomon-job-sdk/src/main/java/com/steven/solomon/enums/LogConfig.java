package com.steven.solomon.enums;


/**
 * 任务日志配置
 *
 * @author yhz
 * @since 2022/9/16
 */
public class LogConfig {
    /**
     * log type {@link tech.powerjob.common.enums.LogType}
     */
    private Integer type;
    /**
     * log level {@link tech.powerjob.common.enums.LogLevel}
     */
    private Integer level;

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
