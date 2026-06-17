package com.steven.solomon.security.core.permission;

import cn.hutool.core.util.StrUtil;

/**
 * 权限编码生成器。
 *
 * <p>根据接口路径自动生成权限编码：去除 {@code /api} 前缀，按 {@code /} 分段，
 * 每段转大写并去除路径变量花括号，用 {@code :} 连接。</p>
 *
 * <p>示例：{@code /api/core/orders/{id}} → {@code CORE:ORDERS:ID}</p>
 *
 * @author steven
 */
public final class PermissionCodeGenerator {

    private PermissionCodeGenerator() {
    }

    /**
     * 根据接口路径生成权限编码。
     *
     * @param path 接口路径
     * @return 权限编码
     * @throws IllegalArgumentException 路径为空
     */
    public static String fromPath(String path) {
        if (StrUtil.isBlank(path)) {
            throw new IllegalArgumentException("权限路径不能为空");
        }
        String normalized = StrUtil.removePrefix(path, "/");
        if (StrUtil.startWithIgnoreCase(normalized, "api/")) {
            normalized = normalized.substring(4);
        }
        String[] segments = normalized.split("/");
        StringBuilder sb = new StringBuilder();
        for (String segment : segments) {
            if (StrUtil.isBlank(segment)) {
                continue;
            }
            String cleaned = segment.replace("{", "").replace("}", "");
            if (StrUtil.isBlank(cleaned)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(":");
            }
            sb.append(cleaned.toUpperCase());
        }
        return sb.toString();
    }
}
