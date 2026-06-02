package com.steven.solomon.properties;

import com.steven.solomon.enums.JobRegisterFailureStrategy;
import com.steven.solomon.enums.JobRegisterMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * PowerJob 后台自动注册配置。
 *
 * <p>Worker 启动参数由 PowerJob 官方 starter 管理，当前配置仅用于后台登录和任务注册。</p>
 */
@ConfigurationProperties("powerjob.worker.register")
public class PowerJobRegisterProperties {

    private String userName;

    private String password;

    private String namespace;

    private boolean enabled = false;

    private JobRegisterMode mode = JobRegisterMode.UPSERT;

    private JobRegisterFailureStrategy failureStrategy = JobRegisterFailureStrategy.FAIL_FAST;

    private boolean autoCreateNamespaceApp = true;

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

    public boolean getAutoCreateNamespaceApp() {
        return autoCreateNamespaceApp;
    }

    public void setAutoCreateNamespaceApp(boolean autoCreateNamespaceApp) {
        this.autoCreateNamespaceApp = autoCreateNamespaceApp;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
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
