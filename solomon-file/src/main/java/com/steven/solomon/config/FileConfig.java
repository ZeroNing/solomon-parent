package com.steven.solomon.config;

import com.steven.solomon.namingRules.DateNamingRulesGenerationService;
import com.steven.solomon.namingRules.FileNamingRulesGenerationService;
import com.steven.solomon.namingRules.OriginalNamingRulesGenerationService;
import com.steven.solomon.namingRules.SnowflakeNamingRulesGenerationService;
import com.steven.solomon.namingRules.UUIDNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.service.BOSServiceInterface;
import com.steven.solomon.service.COSServiceInterface;
import com.steven.solomon.service.DefaultServiceInterface;
import com.steven.solomon.service.FileServiceInterface;
import com.steven.solomon.service.KODOServiceInterface;
import com.steven.solomon.service.MinioServiceInterface;
import com.steven.solomon.service.OBSServiceInterface;
import com.steven.solomon.service.OSSServiceInterface;
import com.steven.solomon.utils.logger.LoggerUtils;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(value={FileChoiceProperties.class})
public class FileConfig {

  private final Logger logger = LoggerUtils.logger(FileConfig.class);

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

  @Bean
  @ConditionalOnMissingBean(FileServiceInterface.class)
  @ConditionalOnClass(OkHttpClient.Builder.class)
  public FileServiceInterface fileService(FileChoiceProperties fileProperties){
    logger.info("选择 {} 文件服务", fileProperties.getChoice());
    switch (fileProperties.getChoice()) {
      case MINIO:
        return new MinioServiceInterface(fileProperties);
      case OSS:
        return new OSSServiceInterface(fileProperties);
      case OBS:
        return new OBSServiceInterface(fileProperties);
      case COS:
        return new COSServiceInterface(fileProperties);
      case BOS:
        return new BOSServiceInterface(fileProperties);
      case KODO:
        return new KODOServiceInterface(fileProperties);
      default:
        return new DefaultServiceInterface();
    }
  }
}
