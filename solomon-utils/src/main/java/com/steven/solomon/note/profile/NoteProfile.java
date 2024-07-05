package com.steven.solomon.note.profile;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("note")
public class NoteProfile {

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
