package com.steven.solomon.config;

import com.steven.solomon.clamav.utils.ClamAvUtils;
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
  public FileServiceInterface fileService(FileChoiceProperties fileProperties, FileNamingRulesGenerationService fileNamingRulesGenerationService, ClamAvUtils clamAvUtils){
    logger.info("选择 {} 文件服务", fileProperties.getChoice().getDesc());
    switch (fileProperties.getChoice()) {
      case MINIO:
        return new MinioService(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case OSS:
        return new OSSService(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case OBS:
        return new OBSService(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case COS:
        return new COSService(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case BOS:
        return new BOSService(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case KODO:
        return new KODOService(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case ZOS:
        return new ZOSService(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case KS3:
        return new KS3Service(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case EOS:
        return new EOSService(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case NOS:
        return new NOSService(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case B2:
        return new B2Service(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case JD:
        return new JDService(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case YANDEX:
        return new YandexService(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case AMAZON:
        return new AmazonService(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case SHARKTECH:
        return new SharktechService(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case DIDI:
        return new DiDiService(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case BOTO3:
        return new Boto3Service(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case TOS:
        return new TosService(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case R2:
        return new R2Service(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case GOOGLE_CLOUD_STORAGE:
        return new GoogleCloudStorageService(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case UOS:
        return new UosService(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case AZURE:
        return new AzureService(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case INSPUR:
        return new InspurService(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      case S3:
        return new S3Service(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
      default:
        return new DefaultService(fileProperties,fileNamingRulesGenerationService,clamAvUtils);
    }
  }
}
