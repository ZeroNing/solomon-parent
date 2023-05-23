package com.steven.solomon.config;

import com.steven.solomon.service.KODOService;
import com.steven.solomon.utils.logger.LoggerUtils;
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
import com.steven.solomon.service.MinioService;
import com.steven.solomon.service.OBSService;
import com.steven.solomon.service.OSSService;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration(proxyBeanMethods = false)
@Order(2)
@EnableConfigurationProperties(value={FileChoiceProperties.class})
public class FileConfig {

  private final Logger logger = LoggerUtils.logger(FileConfig.class);

  @Bean
  @ConditionalOnMissingBean(FileServiceInterface.class)
  public FileServiceInterface fileService(FileChoiceProperties fileProperties){
    logger.info("选择 {} 文件服务", fileProperties.getChoice());
    switch (fileProperties.getChoice()) {
      case MINIO:
        return new MinioService(fileProperties);
      case OSS:
        return new OSSService(fileProperties);
      case OBS:
        return new OBSService(fileProperties);
      case COS:
        return new COSService(fileProperties);
      case BOS:
        return new BOSService(fileProperties);
      case KODO:
        return new KODOService(fileProperties);
      default:
        return new DefaultService();
    }
  }

  @Bean
  @ConditionalOnMissingBean(FileNamingRulesGenerationService.class)
  public FileNamingRulesGenerationService fileNamingMethodService(FileChoiceProperties fileProperties){
    logger.info("选择 {} 命名规则", fileProperties.getFileNamingMethod());
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
