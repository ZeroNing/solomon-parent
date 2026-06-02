package com.steven.solomon.config;

import com.steven.solomon.clamav.utils.ClamAvUtils;
import com.steven.solomon.naming.rules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.service.AbstractFileService;
import com.steven.solomon.service.FileServiceInterface;
import com.steven.solomon.service.AmazonS3Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * S3 协议兼容对象存储自动配置。
 */
@Configuration
@ConditionalOnClass(S3Client.class)
@Conditional(S3CompatibleStorageCondition.class)
public class S3AutoConfig {

  /**
   * 注册 S3 协议兼容对象存储服务 Bean。
   *
   * <p>当 classpath 中存在 {@link S3Client} 且 {@code file.choice} 匹配 S3 兼容供应商时生效。
   * 在容器中不存在 {@link FileServiceInterface} 类型的 Bean 时才会注册。</p>
   *
   * @param properties     文件存储配置属性
   * @param fileNamingRule 文件命名规则
   * @param clamAvUtils    ClamAV 病毒扫描工具
   * @return S3 文件服务实例
   * @throws Exception 初始化或检查默认桶时抛出
   */
  @Bean
  @ConditionalOnMissingBean(FileServiceInterface.class)
  public FileServiceInterface s3FileService(
      FileChoiceProperties properties,
      FileNamingRulesGenerationService fileNamingRule,
      ClamAvUtils clamAvUtils) throws Exception {
    FileStorageConfigValidator.requireProviderConfig(properties, "Amazon S3");
    AbstractFileService service = new AmazonS3Service(properties, fileNamingRule, clamAvUtils);
    service.checkDefaultBucketOnStartup();
    return service;
  }
}
