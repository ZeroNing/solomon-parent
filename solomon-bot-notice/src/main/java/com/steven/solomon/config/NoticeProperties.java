package com.steven.solomon.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 机器人通知服务配置
 * 配置前缀：solomon.notice
 */
@ConfigurationProperties(prefix = "solomon.notice")
public class NoticeProperties {

    /**
     * 是否启用通知服务，默认：true
     * 设为false时所有通知都不会发送
     */
    private boolean enabled = true;

    /**
     * 是否开启全局签名，默认：true
     * 开启后所有消息末尾会自动加上签名内容
     */
    private boolean globalSignature = true;

    /**
     * 全局签名内容，默认：【Solomon通知中心】
     * 会自动追加到所有消息的末尾
     */
    private String signature = "【Solomon通知中心】";

    /**
     * 企业微信机器人配置
     */
    private WechatWork wechatWork;

    /**
     * 钉钉机器人配置
     */
    private DingTalk dingTalk;

    /**
     * 飞书机器人配置
     */
    private Feishu feishu;

    /**
     * 企业微信机器人配置
     * 机器人创建地址：企业微信->群设置->添加群机器人->新建自定义机器人
     */
    public static class WechatWork {
        /**
         * 企业微信机器人Webhook地址
         * 格式：https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=你的机器人key
         */
        private String webhookUrl;
        /**
         * 签名密钥（可选）
         * 机器人安全设置中开启「签名验证」时需要配置
         */
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
     * 钉钉机器人配置
     * 机器人创建地址：钉钉->群设置->智能群助手->添加机器人->自定义机器人
     */
    public static class DingTalk {
        /**
         * 钉钉机器人Webhook地址
         * 格式：https://oapi.dingtalk.com/robot/send?access_token=你的机器人token
         */
        private String webhookUrl;
        /**
         * 签名密钥（可选）
         * 机器人安全设置中开启「加签」时需要配置
         */
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
     * 飞书机器人配置
     * 机器人创建地址：飞书->群设置->群机器人->添加机器人->自定义机器人
     */
    public static class Feishu {
        /**
         * 飞书机器人Webhook地址
         * 格式：https://open.feishu.cn/open-apis/bot/v2/hook/你的机器人hook_key
         */
        private String webhookUrl;
        /**
         * 签名密钥（可选）
         * 机器人安全设置中开启「签名校验」时需要配置
         */
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
