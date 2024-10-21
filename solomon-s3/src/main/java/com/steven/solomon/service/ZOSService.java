package com.steven.solomon.service;

import com.steven.solomon.properties.FileChoiceProperties;

/**
 * 天翼云文件实现类
 */
public class ZOSService extends S3Service {

  public ZOSService(FileChoiceProperties properties) {
    super(properties);
  }

}
