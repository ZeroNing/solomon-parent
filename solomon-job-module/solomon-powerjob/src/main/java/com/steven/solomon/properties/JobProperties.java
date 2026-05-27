package com.steven.solomon.properties;

import com.steven.solomon.enums.JobRegisterFailureStrategy;
import com.steven.solomon.enums.JobRegisterMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("powerjob.worker")
public class JobProperties {

    private String userName;

    private String password;

    private String namespace;

    //是否自动注册
    private boolean autoRegister = false;

    // 自动注册写入模式
    private JobRegisterMode registerMode = JobRegisterMode.UPSERT;

    // 自动注册失败策略
    private JobRegisterFailureStrategy failureStrategy = JobRegisterFailureStrategy.FAIL_FAST;

    // 命名空间和应用不存在时是否自动创建
    private boolean autoCreateNamespaceApp = true;

    public boolean getAutoRegister() {
        return autoRegister;
    }

    public JobRegisterMode getRegisterMode() {
        return registerMode;
    }

    public void setRegisterMode(JobRegisterMode registerMode) {
        this.registerMode = registerMode;
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

    public void setAutoRegister(boolean autoRegister) {
        this.autoRegister = autoRegister;
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
