package com.steven.solomon.note.profile;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

@ConfigurationProperties("bot.note")
public class BotNoteProfile {

    private WeComNotProfile weCom;

    private DingTalkNotProfile dingTalk;

    public static class WeComNotProfile implements Serializable {
        /**
         * 发送消息websocket地址
         */
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    public static class DingTalkNotProfile implements Serializable{
        /**
         * 发送消息websocket地址
         */
        private String url;

        /**
         * 密钥
         */
        private String secret;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

    public WeComNotProfile getWeCom() {
        return weCom;
    }

    public void setWeCom(WeComNotProfile weCom) {
        this.weCom = weCom;
    }

    public DingTalkNotProfile getDingTalk() {
        return dingTalk;
    }

    public void setDingTalk(DingTalkNotProfile dingTalk) {
        this.dingTalk = dingTalk;
    }
}
