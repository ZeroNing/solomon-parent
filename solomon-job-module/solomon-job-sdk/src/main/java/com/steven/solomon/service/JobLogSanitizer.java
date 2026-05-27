package com.steven.solomon.service;

import cn.hutool.json.JSONUtil;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Job 模块日志脱敏工具。
 */
public final class JobLogSanitizer {

    private static final String MASK = "******";

    private JobLogSanitizer() {
    }

    /**
     * 对请求参数中的敏感字段脱敏，避免异常日志泄露凭证。
     */
    public static String sanitize(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return "{}";
        }
        Map<String, Object> target = new LinkedHashMap<>();
        source.forEach((key, value) -> target.put(key, sensitive(key) ? MASK : value));
        return JSONUtil.toJsonStr(target);
    }

    private static boolean sensitive(String key) {
        String lowerKey = key.toLowerCase();
        return lowerKey.contains("password")
                || lowerKey.contains("token")
                || lowerKey.contains("cookie")
                || lowerKey.contains("secret")
                || lowerKey.contains("accesskey")
                || lowerKey.contains("access-key");
    }
}
