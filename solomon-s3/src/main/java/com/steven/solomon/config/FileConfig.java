package com.steven.solomon.config;

import com.steven.solomon.namingRules.DateNamingRulesGenerationService;
import com.steven.solomon.namingRules.FileNamingRulesGenerationService;
import com.steven.solomon.namingRules.OriginalNamingRulesGenerationService;
import com.steven.solomon.namingRules.SnowflakeNamingRulesGenerationService;
import com.steven.solomon.namingRules.UUIDNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.service.*;
import com.steven.solomon.utils.logger.LoggerUtils;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value={FileChoiceProperties.class})
public class FileConfig {

  private final Logger logger = LoggerUtils.logger(FileConfig.class);

  @Bean
  @ConditionalOnMissingBean(FileNamingRulesGenerationService.class)
  public FileNamingRulesGenerationService fileNamingMethodService(FileChoiceProperties fileProperties){
    logger.info("选择 {} 命名规则", fileProperties.getFileNamingMethod().getDesc());
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
    logger.info("选择 {} 文件服务", fileProperties.getChoice().getDesc());
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
      case ZOS:
        return new ZOSService(fileProperties);
      case KS3:
        return new KS3Service(fileProperties);
      case EOS:
        return new EOSService(fileProperties);
      case NOS:
        return new NOSService(fileProperties);
      case B2:
        return new B2Service(fileProperties);
      case JD:
        return new JDService(fileProperties);
      case YANDEX:
        return new YandexService(fileProperties);
      case AMAZON:
        return new AmazonService(fileProperties);
      case SHARKTECH:
        return new SharktechService(fileProperties);
      case DIDI:
        return new DiDiService(fileProperties);
      case BOTO3:
        return new Boto3Service(fileProperties);
      case TOS:
        return new TosService(fileProperties);
      case R2:
        return new R2Service(fileProperties);
      case GOOGLE_CLOUD_STORAGE:
        return new GoogleCloudStorageService(fileProperties);
      case UOS:
        return new UosService(fileProperties);
      case AZURE:
        return new AzureService(fileProperties);
      case INSPUR:
        return new InspurService(fileProperties);
      case S3:
        return new S3Service(fileProperties);
      default:
        return new DefaultService(fileProperties);
    }
  }
}
