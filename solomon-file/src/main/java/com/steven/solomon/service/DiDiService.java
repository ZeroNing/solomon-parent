package com.steven.solomon.service;

import com.steven.solomon.properties.FileChoiceProperties;

/**
 *  滴滴云存储文件实现类
 */
public class DiDiService extends S3Service {

  public DiDiService(FileChoiceProperties properties) {
    super(properties);
  }

}
