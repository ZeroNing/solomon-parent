package com.steven.solomon.service;

import cn.hutool.core.util.StrUtil;
import com.steven.solomon.entity.NoticeMessage;
import com.steven.solomon.verification.ValidateUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Base64;

/**
 * 机器人消息体构建公共工具。
 *
 * <p>钉钉、飞书、企业微信的报文结构不同，但签名计算、媒体资源包装、
 * 按钮/用户列表判空等基础逻辑一致。集中到这里可以避免各渠道重复实现，
 * 后续新增机器人渠道时也只需要关注平台差异。</p>
 */
final class NoticePayloadUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private NoticePayloadUtils() {
    }

    /**
     * 判断列表是否有有效元素。
     */
    static boolean hasItems(List<?> list) {
        return ValidateUtils.isNotEmpty(list);
    }

    /**
     * 构建只有 media_id 的通用媒体消息体。
     */
    static Map<String, Object> mediaIdBody(String mediaId) {
        Map<String, Object> body = new HashMap<>(1);
        body.put("media_id", mediaId);
        return body;
    }

    /**
     * 在消息末尾追加全局签名。
     *
     * @param content 原始内容，允许为空
     * @param enabled 是否开启全局签名
     * @param signature 签名内容
     * @param prefix 签名前缀，例如钉钉 Markdown 使用 {@code "\n> "}
     * @return 拼接后的内容
     */
    static String appendSignature(String content, boolean enabled, String signature, String prefix) {
        StringBuilder builder = new StringBuilder(StrUtil.nullToEmpty(content));
        if (enabled && StrUtil.isNotBlank(signature)) {
            builder.append(StrUtil.nullToEmpty(prefix)).append(signature);
        }
        return builder.toString();
    }

    /**
     * 使用 HmacSHA256 生成 Base64 签名。
     */
    static String hmacSha256Base64(String content, String secret) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
        byte[] signData = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signData);
    }

    /**
     * 获取第一个按钮的跳转地址，按钮为空时返回默认地址。
     */
    static String firstButtonUrlOrDefault(NoticeMessage message, String defaultUrl) {
        if (!hasItems(message.getButtons())) {
            return defaultUrl;
        }
        return message.getButtons().get(0).getActionUrl();
    }
}
