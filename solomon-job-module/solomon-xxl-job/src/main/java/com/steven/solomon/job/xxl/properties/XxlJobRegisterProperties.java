package com.steven.solomon.job.xxl.properties;

import com.steven.solomon.enums.JobRegisterFailureStrategy;
import com.steven.solomon.enums.JobRegisterMode;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** XXL-JOB 后台自动注册配置。 */
@ConfigurationProperties("xxl.register")
@Validated
public class XxlJobRegisterProperties {

    /** XXL-JOB 管理端登录用户名。 */
    private String userName;

    /** XXL-JOB 管理端登录密码。 */
    private String password;

    /** 是否启用自动注册，默认关闭。 */
    private boolean enabled = false;

    /** 自动注册写入模式，默认 UPSERT（不存在创建、存在更新）。 */
    @NotNull(message = "xxl.register.mode must not be null")
    private JobRegisterMode mode = JobRegisterMode.UPSERT;

    /** 注册失败处理策略，默认 FAIL_FAST（快速失败）。 */
    @NotNull(message = "xxl.register.failure-strategy must not be null")
    private JobRegisterFailureStrategy failureStrategy = JobRegisterFailureStrategy.FAIL_FAST;

    /** 是否自动解析执行器组 ID（按 appName 匹配）。 */
    private boolean autoResolveJobGroup = true;

    /** 更新任务时是否同步启停状态，默认 false。 */
    private boolean syncStatusOnUpdate = false;

    @Min(value = 1, message = "xxl.register.admin-api-max-attempts must be at least 1")
    private int adminApiMaxAttempts = 1;

    @Min(value = 0, message = "xxl.register.admin-api-backoff-millis must not be negative")
    private long adminApiBackoffMillis = 0;

    @Min(value = 1, message = "xxl.register.admin-api-circuit-failure-threshold must be at least 1")
    private int adminApiCircuitFailureThreshold = 5;

    @Min(value = 1, message = "xxl.register.admin-api-circuit-open-duration-millis must be at least 1")
    private long adminApiCircuitOpenDurationMillis = 30000;

    @AssertTrue(message = "xxl.register.user-name and xxl.register.password are required when xxl.register.enabled=true")
    public boolean isEnabledConfigurationValid() {
        return !enabled || (hasText(userName) && hasText(password));
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
