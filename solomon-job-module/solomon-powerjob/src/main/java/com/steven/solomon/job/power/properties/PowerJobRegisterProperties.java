package com.steven.solomon.job.power.properties;

import com.steven.solomon.enums.JobRegisterFailureStrategy;
import com.steven.solomon.enums.JobRegisterMode;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * PowerJob 后台自动注册配置。
 *
 * <p>Worker 启动参数由 PowerJob 官方 starter 管理，当前配置仅用于后台登录和任务注册。</p>
 */
@ConfigurationProperties("powerjob.worker.register")
@Validated
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
    @NotNull(message = "powerjob.worker.register.mode must not be null")
    private JobRegisterMode mode = JobRegisterMode.UPSERT;

    /** 注册失败处理策略，默认 FAIL_FAST（快速失败）。 */
    @NotNull(message = "powerjob.worker.register.failure-strategy must not be null")
    private JobRegisterFailureStrategy failureStrategy = JobRegisterFailureStrategy.FAIL_FAST;

    /** 是否自动创建不存在的命名空间和应用，默认 true。 */
    private boolean autoCreateNamespaceApp = true;

    @Min(value = 1, message = "powerjob.worker.register.admin-api-max-attempts must be at least 1")
    private int adminApiMaxAttempts = 1;

    @Min(value = 0, message = "powerjob.worker.register.admin-api-backoff-millis must not be negative")
    private long adminApiBackoffMillis = 0;

    @Min(value = 1, message = "powerjob.worker.register.admin-api-circuit-failure-threshold must be at least 1")
    private int adminApiCircuitFailureThreshold = 5;

    @Min(value = 1, message = "powerjob.worker.register.admin-api-circuit-open-duration-millis must be at least 1")
    private long adminApiCircuitOpenDurationMillis = 30000;

    @AssertTrue(message = "powerjob.worker.register.user-name, password and namespace are required when powerjob.worker.register.enabled=true")
    public boolean isRegisterConfigValid() {
        return !enabled || (hasText(userName) && hasText(password) && hasText(namespace));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

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

    public int getAdminApiMaxAttempts() {
        return adminApiMaxAttempts;
    }

    public void setAdminApiMaxAttempts(int adminApiMaxAttempts) {
        this.adminApiMaxAttempts = adminApiMaxAttempts;
    }

    public long getAdminApiBackoffMillis() {
        return adminApiBackoffMillis;
    }

    public void setAdminApiBackoffMillis(long adminApiBackoffMillis) {
        this.adminApiBackoffMillis = adminApiBackoffMillis;
    }

    public int getAdminApiCircuitFailureThreshold() {
        return adminApiCircuitFailureThreshold;
    }

    public void setAdminApiCircuitFailureThreshold(int adminApiCircuitFailureThreshold) {
        this.adminApiCircuitFailureThreshold = adminApiCircuitFailureThreshold;
    }

    public long getAdminApiCircuitOpenDurationMillis() {
        return adminApiCircuitOpenDurationMillis;
    }

    public void setAdminApiCircuitOpenDurationMillis(long adminApiCircuitOpenDurationMillis) {
        this.adminApiCircuitOpenDurationMillis = adminApiCircuitOpenDurationMillis;
    }
}
