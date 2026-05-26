package com.steven.solomon.config;

import com.qcloud.cos.COSClient;
import com.steven.solomon.clamav.utils.ClamAvUtils;
import com.steven.solomon.naming.rules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.service.COSService;
import com.steven.solomon.service.FileServiceInterface;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云 COS 自动配置。
 */
@Configuration
@ConditionalOnClass(COSClient.class)
@ConditionalOnProperty(prefix = "file", name = "choice", havingValue = "COS")
public class CosAutoConfig {

  @Bean
  @ConditionalOnMissingBean(FileServiceInterface.class)
  public FileServiceInterface cosFileService(
      FileChoiceProperties properties,
      FileNamingRulesGenerationService fileNamingRule,
      ClamAvUtils clamAvUtils) {
    return new COSService(properties, fileNamingRule, clamAvUtils);
  }
}
