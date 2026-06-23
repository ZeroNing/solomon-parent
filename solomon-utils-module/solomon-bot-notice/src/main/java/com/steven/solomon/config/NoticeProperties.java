package com.steven.solomon.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 机器人通知配置。
 */
@ConfigurationProperties(prefix = "solomon.notice")
@Validated
public class NoticeProperties {

    /** 是否启用通知服务。 */
    private boolean enabled = true;

    /** 是否启用全局签名。 */
    private boolean globalSignature = true;

    /** 全局签名内容。 */
    private String signature = "【Solomon 通知中心】";

    /** 企业微信机器人配置。 */
    @Valid
    private WechatWork wechatWork;

    /** 钉钉机器人配置。 */
    @Valid
    private DingTalk dingTalk;

    /** 飞书机器人配置。 */
    @Valid
    private Feishu feishu;

    @Min(value = 1, message = "solomon.notice.async-core-pool-size must be at least 1")
    private int asyncCorePoolSize = 2;

    @Min(value = 1, message = "solomon.notice.async-max-pool-size must be at least 1")
    private int asyncMaxPoolSize = 8;

    @Min(value = 1, message = "solomon.notice.async-queue-capacity must be at least 1")
    private int asyncQueueCapacity = 200;

    @Min(value = 1, message = "solomon.notice.async-keep-alive-seconds must be at least 1")
    private int asyncKeepAliveSeconds = 60;

    @Min(value = 100, message = "solomon.notice.send-timeout-millis must be at least 100")
    private int sendTimeoutMillis = 5000;

    @Min(value = 0, message = "solomon.notice.retry-backoff-millis must not be negative")
    private int retryBackoffMillis = 2000;

    /**
     * Webhook 机器人基础配置。
     */
    public static class WebhookConfig {

        /** 机器人 Webhook 地址。 */
        @NotBlank(message = "webhookUrl must not be blank when webhook config is present")
        private String webhookUrl;

        /** 机器人签名密钥。 */
        private String secret;

        public String getWebhookUrl() {
            return webhookUrl;
        }

        public void setWebhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

    /**
     * 企业微信机器人配置。
     */
    public static class WechatWork extends WebhookConfig {
    }

    /**
     * 钉钉机器人配置。
     */
    public static class DingTalk extends WebhookConfig {
    }

    /**
     * 飞书机器人配置。
     */
    public static class Feishu extends WebhookConfig {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isGlobalSignature() {
        return globalSignature;
    }

    public void setGlobalSignature(boolean globalSignature) {
        this.globalSignature = globalSignature;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getAsyncCorePoolSize() {
        return asyncCorePoolSize;
    }

    public void setAsyncCorePoolSize(int asyncCorePoolSize) {
        this.asyncCorePoolSize = asyncCorePoolSize;
    }

    public int getAsyncMaxPoolSize() {
        return asyncMaxPoolSize;
    }

    public void setAsyncMaxPoolSize(int asyncMaxPoolSize) {
        this.asyncMaxPoolSize = asyncMaxPoolSize;
    }

    public int getAsyncQueueCapacity() {
        return asyncQueueCapacity;
    }

    public void setAsyncQueueCapacity(int asyncQueueCapacity) {
        this.asyncQueueCapacity = asyncQueueCapacity;
    }

    public int getAsyncKeepAliveSeconds() {
        return asyncKeepAliveSeconds;
    }

    public void setAsyncKeepAliveSeconds(int asyncKeepAliveSeconds) {
        this.asyncKeepAliveSeconds = asyncKeepAliveSeconds;
    }

    public int getSendTimeoutMillis() {
        return sendTimeoutMillis;
    }

    public void setSendTimeoutMillis(int sendTimeoutMillis) {
        this.sendTimeoutMillis = sendTimeoutMillis;
    }

    public int getRetryBackoffMillis() {
        return retryBackoffMillis;
    }

    public void setRetryBackoffMillis(int retryBackoffMillis) {
        this.retryBackoffMillis = retryBackoffMillis;
    }

    public WechatWork getWechatWork() {
        return wechatWork;
    }

    public void setWechatWork(WechatWork wechatWork) {
        this.wechatWork = wechatWork;
    }

    public DingTalk getDingTalk() {
        return dingTalk;
    }

    public void setDingTalk(DingTalk dingTalk) {
        this.dingTalk = dingTalk;
    }

    public Feishu getFeishu() {
        return feishu;
    }

    public void setFeishu(Feishu feishu) {
        this.feishu = feishu;
    }
}
