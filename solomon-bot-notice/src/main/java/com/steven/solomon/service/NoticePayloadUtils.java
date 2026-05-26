package com.steven.solomon.service;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.entity.NoticeMessage;
import com.steven.solomon.verification.ValidateUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 机器人报文构造工具。
 *
 * <p>平台报文结构不同，但签名、媒体字段、空列表判断等基础逻辑一致，集中在这里避免重复实现。</p>
 */
final class NoticePayloadUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private NoticePayloadUtils() {
    }

    static PayloadMap map() {
        return new PayloadMap();
    }

    static PayloadMap map(String key, Object value) {
        return map().put(key, value);
    }

    static boolean hasItems(List<?> list) {
        return ValidateUtils.isNotEmpty(list);
    }

    static Map<String, Object> mediaIdBody(String mediaId) {
        return map("media_id", mediaId).build();
    }

    static String extString(NoticeMessage message, String key) {
        if (message == null || message.getExtParams() == null || !message.getExtParams().containsKey(key)) {
            return null;
        }
        Object value = message.getExtParams().get(key);
        return value == null ? null : String.valueOf(value);
    }

    static Integer extInteger(NoticeMessage message, String key) {
        String value = extString(message, key);
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    static String appendSignature(String content, boolean enabled, String signature, String prefix) {
        StringBuilder builder = new StringBuilder(StrUtil.nullToEmpty(content));
        if (enabled && StrUtil.isNotBlank(signature)) {
            builder.append(StrUtil.nullToEmpty(prefix)).append(signature);
        }
        return builder.toString();
    }

    static String hmacSha256Base64(String content, String secret) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
        byte[] signData = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signData);
    }

    static String firstButtonUrlOrDefault(NoticeMessage message, String defaultUrl) {
        if (!hasItems(message.getButtons())) {
            return defaultUrl;
        }
        return message.getButtons().get(0).getActionUrl();
    }

    /**
     * 小型 Map 链式构造器，专门服务机器人 JSON 报文。
     */
    static final class PayloadMap {

        private final Map<String, Object> data = new HashMap<>();

        PayloadMap put(String key, Object value) {
            data.put(key, value);
            return this;
        }

        PayloadMap putIf(boolean condition, String key, Object value) {
            if (condition) {
                data.put(key, value);
            }
            return this;
        }

        Map<String, Object> build() {
            return data;
        }
    }
}
