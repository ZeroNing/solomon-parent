package com.steven.solomon.cache.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 缓存摘要工具。
 *
 * <p>统一使用 SHA-256 摘要算法生成长度固定且稳定的缓存 key 片段，
 * 用于在缓存 key 无法直接暴露或需要压缩长字符串时生成安全摘要。</p>
 */
public final class CacheDigestUtils {

  private CacheDigestUtils() {
  }

  /**
   * 对输入字符串进行 SHA-256 摘要，返回十六进制字符串。
   *
   * @param source 待摘要的原始字符串
   * @return 64 位十六进制摘要字符串
   * @throws IllegalStateException 当前运行环境不支持 SHA-256 时抛出
   */
  public static String sha256(String source) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] bytes = digest.digest(source.getBytes(StandardCharsets.UTF_8));
      StringBuilder builder = new StringBuilder(bytes.length * 2);
      for (byte item : bytes) {
        builder.append(String.format("%02x", item));
      }
      return builder.toString();
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("当前运行环境不支持 SHA-256", ex);
    }
  }
}
