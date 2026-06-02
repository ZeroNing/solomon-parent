package com.steven.solomon.config;

import com.baidubce.services.bos.BosClient;
import com.steven.solomon.clamav.utils.ClamAvUtils;
import com.steven.solomon.naming.rules.FileNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.service.AbstractFileService;
import com.steven.solomon.service.BOSService;
import com.steven.solomon.service.FileServiceInterface;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 百度云 BOS 自动配置。
 */
@Configuration
@ConditionalOnClass(BosClient.class)
@ConditionalOnProperty(prefix = "file", name = "choice", havingValue = "BOS")
public class BosAutoConfig {

  /**
   * 注册百度云 BOS 文件服务 Bean。
   *
   * <p>当 classpath 中存在 {@link BosClient} 且配置 {@code file.choice=BOS} 时生效。</p>
   *
   * @param properties     文件存储配置属性
   * @param fileNamingRule 文件命名规则
   * @param clamAvUtils    ClamAV 病毒扫描工具
   * @return BOS 文件服务实例
   * @throws Exception 初始化或检查默认桶时抛出
   */
  @Bean
  @ConditionalOnMissingBean(FileServiceInterface.class)
  public FileServiceInterface bosFileService(
      FileChoiceProperties properties,
      FileNamingRulesGenerationService fileNamingRule,
      ClamAvUtils clamAvUtils) throws Exception {
    FileStorageConfigValidator.requireProviderConfig(properties, "BOS");
    AbstractFileService service = new BOSService(properties, fileNamingRule, clamAvUtils);
    service.checkDefaultBucketOnStartup();
    return service;
  }
}
