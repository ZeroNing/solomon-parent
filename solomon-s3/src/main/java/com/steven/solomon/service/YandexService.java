package com.steven.solomon.service;

import com.steven.solomon.clamav.utils.ClamAvUtils;
import com.steven.solomon.namingRules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;

/**
 *  俄罗斯Yandex云存储文件实现类
 */
public class YandexService extends S3Service {

  public YandexService(FileChoiceProperties properties, FileNamingRulesGenerationService fileNamingRulesGenerationService, ClamAvUtils clamAvUtils) {
    super(properties,fileNamingRulesGenerationService,clamAvUtils);
  }

}
