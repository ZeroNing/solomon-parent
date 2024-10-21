package com.steven.solomon.service;

import com.steven.solomon.properties.FileChoiceProperties;

/**
 *  R2云存储文件实现类
 */
public class R2Service extends S3Service {

  public R2Service(FileChoiceProperties properties) {
    super(properties);
  }

}
