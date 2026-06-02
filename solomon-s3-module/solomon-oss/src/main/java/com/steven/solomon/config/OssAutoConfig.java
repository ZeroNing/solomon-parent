package com.steven.solomon.config;

import com.aliyun.oss.OSS;
import com.steven.solomon.clamav.utils.ClamAvUtils;
import com.steven.solomon.naming.rules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.service.AbstractFileService;
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

  /**
   * 注册阿里云 OSS 文件服务 Bean。
   *
   * <p>当 classpath 中存在 {@link OSS} 且配置 {@code file.choice=OSS} 时生效。</p>
   *
   * @param properties     文件存储配置属性
   * @param fileNamingRule 文件命名规则
   * @param clamAvUtils    ClamAV 病毒扫描工具
   * @return OSS 文件服务实例
   * @throws Exception 初始化或检查默认桶时抛出
   */
  @Bean
  @ConditionalOnMissingBean(FileServiceInterface.class)
  public FileServiceInterface ossFileService(
      FileChoiceProperties properties,
      FileNamingRulesGenerationService fileNamingRule,
      ClamAvUtils clamAvUtils) throws Exception {
    FileStorageConfigValidator.requireProviderConfig(properties, "OSS");
    AbstractFileService service = new OSSService(properties, fileNamingRule, clamAvUtils);
    service.checkDefaultBucketOnStartup();
    return service;
  }
}
