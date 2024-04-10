package com.steven.solomon.service;

import com.steven.solomon.properties.FileChoiceProperties;

/**
 *  亚马逊云存储文件实现类
 */
public class AmazonService extends S3Service {

  public AmazonService(FileChoiceProperties properties) {
    super(properties);
  }

}
