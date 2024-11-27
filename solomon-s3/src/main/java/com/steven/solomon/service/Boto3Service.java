package com.steven.solomon.service;

import com.steven.solomon.namingRules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;

/**
 *  交大云存储文件实现类
 */
public class Boto3Service extends S3Service {

  public Boto3Service(FileChoiceProperties properties, FileNamingRulesGenerationService fileNamingRulesGenerationService) {
    super(properties,fileNamingRulesGenerationService);
  }

}
