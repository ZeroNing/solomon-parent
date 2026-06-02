package com.steven.solomon.properties;

import com.steven.solomon.enums.JobRegisterFailureStrategy;
import com.steven.solomon.enums.JobRegisterMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** XXL-JOB 后台自动注册配置。 */
@ConfigurationProperties("xxl.register")
public class XxlJobRegisterProperties {

    private String userName;

    private String password;

    private boolean enabled = false;

    private JobRegisterMode mode = JobRegisterMode.UPSERT;

    private JobRegisterFailureStrategy failureStrategy = JobRegisterFailureStrategy.FAIL_FAST;

    private boolean autoResolveJobGroup = true;

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
