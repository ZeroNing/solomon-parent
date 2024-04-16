package com.steven.solomon.service;

import com.steven.solomon.properties.FileChoiceProperties;

/**
 * 谷歌云文件实现类
 */
public class GoogleCloudStorageService extends S3Service {

  public GoogleCloudStorageService(FileChoiceProperties properties) {
    super(properties);
  }

}
