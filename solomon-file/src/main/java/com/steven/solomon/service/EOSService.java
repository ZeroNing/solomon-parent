package com.steven.solomon.service;

import com.steven.solomon.properties.FileChoiceProperties;

/**
 * 移动云文件实现类
 */
public class EOSService extends S3Service {

  public EOSService(FileChoiceProperties properties) {
    super(properties);
  }

}
