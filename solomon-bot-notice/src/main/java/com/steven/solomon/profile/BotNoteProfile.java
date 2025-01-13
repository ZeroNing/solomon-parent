package com.steven.solomon.profile;

import com.steven.solomon.verification.ValidateUtils;
import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

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

        public String getWebUrl() throws Exception {
            if(ValidateUtils.isEmpty(url)){
                long timestamp = Instant.now().toEpochMilli();
                String stringToSign = timestamp + "\n" + secret;
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
                byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
                String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
                url = url + "&timestamp=" + timestamp + "&sign=" + sign;
                return url;
            } else {
                return url;
            }
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
