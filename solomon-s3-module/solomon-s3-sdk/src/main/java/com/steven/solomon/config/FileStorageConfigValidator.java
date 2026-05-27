package com.steven.solomon.config;

import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.verification.ValidateUtils;

/**
 * 对象存储配置校验器。
 */
public final class FileStorageConfigValidator {

  private FileStorageConfigValidator() {
  }

  /**
   * 校验供应商通用必填配置，避免客户端初始化后才暴露模糊异常。
   */
  public static void requireProviderConfig(FileChoiceProperties properties, String providerName) {
    require(properties.getEndpoint(), providerName, "file.endpoint");
    require(properties.getAccessKey(), providerName, "file.access-key");
    require(properties.getSecretKey(), providerName, "file.secret-key");
    require(properties.getBucketName(), providerName, "file.bucket-name");
  }

  private static void require(String value, String providerName, String propertyName) {
    if (ValidateUtils.isEmpty(value)) {
      throw new IllegalArgumentException(providerName + " 对象存储缺少必要配置: " + propertyName);
    }
  }
}
