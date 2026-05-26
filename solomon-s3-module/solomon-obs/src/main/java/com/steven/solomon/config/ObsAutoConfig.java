package com.steven.solomon.config;

import com.obs.services.ObsClient;
import com.steven.solomon.clamav.utils.ClamAvUtils;
import com.steven.solomon.naming.rules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.service.FileServiceInterface;
import com.steven.solomon.service.OBSService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 华为云 OBS 自动配置。
 */
@Configuration
@ConditionalOnClass(ObsClient.class)
@ConditionalOnProperty(prefix = "file", name = "choice", havingValue = "OBS")
public class ObsAutoConfig {

  @Bean
  @ConditionalOnMissingBean(FileServiceInterface.class)
  public FileServiceInterface obsFileService(
      FileChoiceProperties properties,
      FileNamingRulesGenerationService fileNamingRule,
      ClamAvUtils clamAvUtils) {
    return new OBSService(properties, fileNamingRule, clamAvUtils);
  }
}
