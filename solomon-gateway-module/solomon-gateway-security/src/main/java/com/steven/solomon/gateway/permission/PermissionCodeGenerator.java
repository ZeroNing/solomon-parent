package com.steven.solomon.gateway.permission;

import cn.hutool.core.util.StrUtil;

/**
 * 权限编码生成器。
 *
 * <p>根据接口路径自动生成权限编码，规则：</p>
 * <ol>
 *   <li>去除前导 {@code /api}。</li>
 *   <li>按 {@code /} 分段。</li>
 *   <li>每段转大写、去除路径变量花括号（{@code {id}} → {@code ID}）。</li>
 *   <li>用 {@code :} 连接。</li>
 * </ol>
 *
 * <p>示例：</p>
 * <ul>
 *   <li>{@code /api/core/orders/{id}} → {@code CORE:ORDERS:ID}</li>
 *   <li>{@code /api/auth/login} → {@code AUTH:LOGIN}</li>
 * </ul>
 *
 * @author steven
 */
public final class PermissionCodeGenerator {

    private PermissionCodeGenerator() {
    }

    /**
     * 根据接口路径生成权限编码。
     *
     * @param path 接口路径，如 {@code /api/core/orders/{id}}
     * @return 权限编码，如 {@code CORE:ORDERS:ID}
     * @throws IllegalArgumentException 路径为空或空白
     */
    public static String fromPath(String path) {
        if (StrUtil.isBlank(path)) {
            throw new IllegalArgumentException("权限路径不能为空");
        }
        String normalized = StrUtil.removePrefix(path, "/");
        // 去除 /api 前缀
        if (StrUtil.startWithIgnoreCase(normalized, "api/")) {
            normalized = normalized.substring(4);
        }
        // 按 / 分段，每段转大写并去除花括号
        String[] segments = normalized.split("/");
        StringBuilder sb = new StringBuilder();
        for (String segment : segments) {
            if (StrUtil.isBlank(segment)) {
                continue;
            }
            // 路径变量 {id} -> ID
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
