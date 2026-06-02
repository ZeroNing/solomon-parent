package com.steven.solomon.gateway.permission;

import cn.hutool.core.util.StrUtil;
import java.util.Arrays;
import java.util.stream.Collectors;

/** 根据接口路径生成稳定的权限编码。 */
public final class ApiPermissionCodeGenerator {

  private ApiPermissionCodeGenerator() {
  }

  /**
   * 将接口路径转换为权限编码。
   *
   * <p>例如 {@code /api/core/orders/{id}} 转换为 {@code CORE:ORDERS:ID}。</p>
   */
  public static String fromPath(String path) {
    if (StrUtil.isBlank(path)) {
      throw new IllegalArgumentException("接口路径不能为空");
    }
    String[] segments = StrUtil.removePrefix(StrUtil.trim(path), "/").split("/");
    int start = segments.length > 0 && "api".equalsIgnoreCase(segments[0]) ? 1 : 0;
    String code = Arrays.stream(segments, start, segments.length)
        .map(ApiPermissionCodeGenerator::normalize)
        .filter(StrUtil::isNotBlank)
        .collect(Collectors.joining(":"));
    if (StrUtil.isBlank(code)) {
      throw new IllegalArgumentException("接口路径无法生成权限编码: " + path);
    }
    return code;
  }

  private static String normalize(String segment) {
    return StrUtil.trim(segment).replace("{", "").replace("}", "")
        .replaceAll("[^A-Za-z0-9]+", "_").toUpperCase();
  }
}
