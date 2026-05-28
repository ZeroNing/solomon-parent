package com.steven.solomon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 机器人通知配置。
 */
@ConfigurationProperties(prefix = "solomon.notice")
public class NoticeProperties {

    /** 是否启用通知服务。 */
    private boolean enabled = true;

    /** 是否启用全局签名。 */
    private boolean globalSignature = true;

    /** 全局签名内容。 */
    private String signature = "【Solomon 通知中心】";

    /** 企业微信机器人配置。 */
    private WechatWork wechatWork;

    /** 钉钉机器人配置。 */
    private DingTalk dingTalk;

    /** 飞书机器人配置。 */
    private Feishu feishu;

    /**
     * Webhook 机器人基础配置。
     */
    public static class WebhookConfig {

        /** 机器人 Webhook 地址。 */
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
