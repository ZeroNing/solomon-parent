package com.steven.solomon.config;

import com.steven.solomon.namingRules.DateNamingRulesGenerationService;
import com.steven.solomon.namingRules.FileNamingRulesGenerationService;
import com.steven.solomon.namingRules.OriginalNamingRulesGenerationService;
import com.steven.solomon.namingRules.SnowflakeNamingRulesGenerationService;
import com.steven.solomon.namingRules.UUIDNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.service.BOSService;
import com.steven.solomon.service.COSService;
import com.steven.solomon.service.DefaultService;
import com.steven.solomon.service.FileServiceInterface;
import com.steven.solomon.service.MinIoService;
import com.steven.solomon.service.OBSService;
import com.steven.solomon.service.OSSService;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class FileConfig {

  @Bean
  public FileServiceInterface fileService(FileChoiceProperties fileProperties){
    switch (fileProperties.getChoice()) {
      case MINIO:
        return new MinIoService(fileProperties);
      case OSS:
        return new OSSService(fileProperties);
      case OBS:
        return new OBSService(fileProperties);
      case COS:
        return new COSService(fileProperties);
      case BOS:
        return new BOSService(fileProperties);
      default:
        return new DefaultService();
    }
  }

  @Bean
  public FileNamingRulesGenerationService fileNamingMethodService(FileChoiceProperties fileProperties){
    switch (fileProperties.getFileNamingMethod()) {
      case DATE:
        return new DateNamingRulesGenerationService();
      case UUID:
        return new UUIDNamingRulesGenerationService();
      case SNOWFLAKE:
        return new SnowflakeNamingRulesGenerationService();
      default:
        return new OriginalNamingRulesGenerationService();
    }
  }
}
