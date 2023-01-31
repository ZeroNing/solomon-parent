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

  @Bean("FileServiceInterface")
  public FileServiceInterface fileService(FileChoiceProperties fileProperties){
    switch (fileProperties.getChoice()) {
      case MINIO:
        return new MinIoService(fileProperties.getMinio());
      case OSS:
        return new OSSService(fileProperties.getOss());
      case OBS:
        return new OBSService(fileProperties.getObs());
      case COS:
        return new COSService(fileProperties.getCos());
      case BOS:
        return new BOSService(fileProperties.getBos());
      default:
        return new DefaultService();
    }
  }

  @Bean("FileNameGenerationService")
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
