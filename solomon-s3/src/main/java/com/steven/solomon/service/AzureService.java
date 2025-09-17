package com.steven.solomon.service;

import com.steven.solomon.clamav.utils.ClamAvUtils;
import com.steven.solomon.namingRules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;

/**
 * 微软文件实现类
 */
public class AzureService extends S3Service {

  public AzureService(FileChoiceProperties properties, FileNamingRulesGenerationService fileNamingRulesGenerationService, ClamAvUtils clamAvUtils) {
    super(properties,fileNamingRulesGenerationService,clamAvUtils);
  }

}
