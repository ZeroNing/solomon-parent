package com.steven.solomon.service;

import com.steven.solomon.properties.FileChoiceProperties;

/**
 * 浪潮云文件实现类
 */
public class InspurService extends S3Service {

  public InspurService(FileChoiceProperties properties) {
    super(properties);
  }

}
