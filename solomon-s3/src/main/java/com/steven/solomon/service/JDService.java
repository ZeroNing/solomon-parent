package com.steven.solomon.service;

import com.steven.solomon.properties.FileChoiceProperties;

/**
 *  京东云存储文件实现类
 */
public class JDService extends S3Service {

  public JDService(FileChoiceProperties properties) {
    super(properties);
  }

}
