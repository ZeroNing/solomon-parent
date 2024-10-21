package com.steven.solomon.service;

import com.steven.solomon.properties.FileChoiceProperties;

/**
 * 金山云文件实现类
 */
public class KS3Service extends S3Service {

  public KS3Service(FileChoiceProperties properties) {
    super(properties);
  }

}
