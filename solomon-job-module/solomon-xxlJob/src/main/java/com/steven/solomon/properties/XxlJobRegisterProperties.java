package com.steven.solomon.properties;

import com.steven.solomon.enums.JobRegisterFailureStrategy;
import com.steven.solomon.enums.JobRegisterMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** XXL-JOB 后台自动注册配置。 */
@ConfigurationProperties("xxl.register")
public class XxlJobRegisterProperties {

    /** XXL-JOB 管理端登录用户名。 */
    private String userName;

    /** XXL-JOB 管理端登录密码。 */
    private String password;

    /** 是否启用自动注册，默认关闭。 */
    private boolean enabled = false;

    /** 自动注册写入模式，默认 UPSERT（不存在创建、存在更新）。 */
    private JobRegisterMode mode = JobRegisterMode.UPSERT;

    /** 注册失败处理策略，默认 FAIL_FAST（快速失败）。 */
    private JobRegisterFailureStrategy failureStrategy = JobRegisterFailureStrategy.FAIL_FAST;

    /** 是否自动解析执行器组 ID（按 appName 匹配）。 */
    private boolean autoResolveJobGroup = true;

    /** 更新任务时是否同步启停状态，默认 false。 */
    private boolean syncStatusOnUpdate = false;

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public JobRegisterMode getMode() {
        return mode;
    }

    public void setMode(JobRegisterMode mode) {
        this.mode = mode;
    }

    public JobRegisterFailureStrategy getFailureStrategy() {
        return failureStrategy;
    }

    public void setFailureStrategy(JobRegisterFailureStrategy failureStrategy) {
        this.failureStrategy = failureStrategy;
    }

    public boolean getAutoResolveJobGroup() {
        return autoResolveJobGroup;
    }

    public void setAutoResolveJobGroup(boolean autoResolveJobGroup) {
        this.autoResolveJobGroup = autoResolveJobGroup;
    }

    public boolean getSyncStatusOnUpdate() {
        return syncStatusOnUpdate;
    }

    public void setSyncStatusOnUpdate(boolean syncStatusOnUpdate) {
        this.syncStatusOnUpdate = syncStatusOnUpdate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
