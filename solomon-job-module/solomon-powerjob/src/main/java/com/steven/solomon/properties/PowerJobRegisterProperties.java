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

    /** PowerJob 管理端登录用户名。 */
    private String userName;

    /** PowerJob 管理端登录密码。 */
    private String password;

    /** 目标命名空间编码。 */
    private String namespace;

    /** 是否启用自动注册，默认关闭。 */
    private boolean enabled = false;

    /** 自动注册写入模式，默认 UPSERT（不存在创建、存在更新）。 */
    private JobRegisterMode mode = JobRegisterMode.UPSERT;

    /** 注册失败处理策略，默认 FAIL_FAST（快速失败）。 */
    private JobRegisterFailureStrategy failureStrategy = JobRegisterFailureStrategy.FAIL_FAST;

    /** 是否自动创建不存在的命名空间和应用，默认 true。 */
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
