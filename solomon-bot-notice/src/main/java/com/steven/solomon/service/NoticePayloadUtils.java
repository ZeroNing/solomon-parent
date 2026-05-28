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

    /** HMAC-SHA256 算法名称。 */
    private static final String HMAC_SHA256 = "HmacSHA256";

    /** 私有构造函数，防止实例化。 */
    private NoticePayloadUtils() {
    }

    /**
     * 创建空的 PayloadMap 链式构造器。
     *
     * @return PayloadMap 实例
     */
    static PayloadMap map() {
        return new PayloadMap();
    }

    /**
     * 创建带初始键值的 PayloadMap 链式构造器。
     *
     * @param key   键名
     * @param value 键值
     * @return PayloadMap 实例
     */
    static PayloadMap map(String key, Object value) {
        return map().put(key, value);
    }

    /**
     * 判断列表是否有元素。
     *
     * @param list 待判断的列表
     * @return true 表示列表非空
     */
    static boolean hasItems(List<?> list) {
        return ValidateUtils.isNotEmpty(list);
    }

    /**
     * 构造媒体ID报文。
     *
     * @param mediaId 媒体资源标识
     * @return 包含 media_id 的 Map
     */
    static Map<String, Object> mediaIdBody(String mediaId) {
        return map("media_id", mediaId).build();
    }

    /**
     * 从扩展参数中取字符串值。
     *
     * @param message 通知消息
     * @param key     扩展参数键名
     * @return 字符串值，不存在时返回 null
     */
    static String extString(NoticeMessage message, String key) {
        if (message == null || message.getExtParams() == null || !message.getExtParams().containsKey(key)) {
            return null;
        }
        Object value = message.getExtParams().get(key);
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 从扩展参数中取整数值。
     *
     * @param message 通知消息
     * @param key     扩展参数键名
     * @return 整数值，不存在或转换失败时返回 null
     */
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

    /**
     * 根据开关配置追加全局签名。
     *
     * @param content    原始内容
     * @param enabled    是否启用签名
     * @param signature  签名内容
     * @param prefix     签名前缀
     * @return 追加签名后的内容
     */
    static String appendSignature(String content, boolean enabled, String signature, String prefix) {
        StringBuilder builder = new StringBuilder(StrUtil.nullToEmpty(content));
        if (enabled && StrUtil.isNotBlank(signature)) {
            builder.append(StrUtil.nullToEmpty(prefix)).append(signature);
        }
        return builder.toString();
    }

    /**
     * 使用 HMAC-SHA256 算法生成签名并编码为 Base64。
     *
     * @param content 待签名内容
     * @param secret  签名密钥
     * @return Base64 编码的签名
     * @throws Exception 签名算法异常
     */
    static String hmacSha256Base64(String content, String secret) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
        byte[] signData = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signData);
    }

    /**
     * 获取第一个按钮跳转URL，无按钮时返回默认URL。
     *
     * @param message    通知消息
     * @param defaultUrl 默认URL
     * @return 跳转URL
     */
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
