package com.steven.solomon.service;

import com.steven.solomon.properties.FileChoiceProperties;

/**
 * 微软文件实现类
 */
public class AzureService extends S3Service {

  public AzureService(FileChoiceProperties properties) {
    super(properties);
  }

}
