package com.steven.solomon.config;

import cn.hutool.core.util.ObjectUtil;

import com.steven.solomon.properties.FileChoiceProperties;

/**
 * 对象存储配置校验器。
 */
public final class FileStorageConfigValidator {

  private FileStorageConfigValidator() {
  }

  /**
   * 校验供应商通用必填配置，避免客户端初始化后才暴露模糊异常。
   *
   * <p>校验以下四项配置不可为空：</p>
   * <ul>
   *   <li>{@code file.endpoint} - 服务端点地址</li>
   *   <li>{@code file.access-key} - 访问密钥</li>
   *   <li>{@code file.secret-key} - 秘密密钥</li>
   *   <li>{@code file.bucket-name} - 默认存储桶名称</li>
   * </ul>
   *
   * @param properties  文件存储配置属性
   * @param providerName 供应商名称（用于错误提示）
   * @throws IllegalArgumentException 如果任何必要配置为空则抛出
   */
  public static void requireProviderConfig(FileChoiceProperties properties, String providerName) {
    require(properties.getEndpoint(), providerName, "file.endpoint");
    require(properties.getAccessKey(), providerName, "file.access-key");
    require(properties.getSecretKey(), providerName, "file.secret-key");
    require(properties.getBucketName(), providerName, "file.bucket-name");
  }

  /**
   * 校验单个配置项不为空。
   *
   * @param value        配置值
   * @param providerName 供应商名称
   * @param propertyName 配置属性名
   * @throws IllegalArgumentException 如果值为空则抛出
   */
  private static void require(String value, String providerName, String propertyName) {
    if (ObjectUtil.isEmpty(value)) {
      throw new IllegalArgumentException(providerName + " 对象存储缺少必要配置: " + propertyName);
    }
  }
}
