package com.steven.solomon.config;

import com.aliyun.oss.OSS;
import com.steven.solomon.clamav.utils.ClamAvUtils;
import com.steven.solomon.naming.rules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.service.FileServiceInterface;
import com.steven.solomon.service.OSSService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 自动配置。
 */
@Configuration
@ConditionalOnClass(OSS.class)
@ConditionalOnProperty(prefix = "file", name = "choice", havingValue = "OSS")
public class OssAutoConfig {

  @Bean
  @ConditionalOnMissingBean(FileServiceInterface.class)
  public FileServiceInterface ossFileService(
      FileChoiceProperties properties,
      FileNamingRulesGenerationService fileNamingRule,
      ClamAvUtils clamAvUtils) {
    return new OSSService(properties, fileNamingRule, clamAvUtils);
  }
}
