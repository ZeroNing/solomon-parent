package com.steven.solomon.entity;

import com.steven.solomon.enums.TaskTrackerBehavior;

/**
 * 任务运行时高级配置
 *
 * @author tjq
 * @since 2024/2/24
 */
public class JobAdvancedRuntimeConfig {

    /**
     * MR 任务专享参数，TaskTracker 行为 {@link tech.powerjob.common.enums.TaskTrackerBehavior}
     */
    private Integer taskTrackerBehavior;

    public Integer getTaskTrackerBehavior() {
        return taskTrackerBehavior;
    }

    public void setTaskTrackerBehavior(Integer taskTrackerBehavior) {
        this.taskTrackerBehavior = taskTrackerBehavior;
    }
}
