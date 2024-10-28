package com.steven.solomon.service;

import com.steven.solomon.properties.FileChoiceProperties;

/**
 * 紫光云文件实现类
 */
public class UosService extends S3Service {

  public UosService(FileChoiceProperties properties) {
    super(properties);
  }

}
