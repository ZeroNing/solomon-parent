package com.steven.solomon.config;

import com.steven.solomon.clamav.utils.ClamAvUtils;
import com.steven.solomon.naming.rules.DateNamingRulesGenerationService;
import com.steven.solomon.naming.rules.FileNamingRulesGenerationService;
import com.steven.solomon.naming.rules.OriginalNamingRulesGenerationService;
import com.steven.solomon.naming.rules.SnowflakeNamingRulesGenerationService;
import com.steven.solomon.naming.rules.UUIDNamingRulesGenerationService;
import com.steven.solomon.properties.FileChoiceProperties;
import com.steven.solomon.service.DefaultService;
import com.steven.solomon.service.FileServiceInterface;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 对象存储 SDK 自动配置。
 *
 * <p>这里只提供供应商无关能力：配置属性、文件命名规则和默认占位服务。真实存储实现由供应商模块提供。</p>
 */
@Configuration
@EnableConfigurationProperties(FileChoiceProperties.class)
public class S3SdkAutoConfig {

  private final Logger logger = LoggerUtils.logger(S3SdkAutoConfig.class);

  /**
   * 根据配置选择文件命名策略，业务侧可以注册同类型 Bean 覆盖默认规则。
   *
   * <p>根据 {@link FileChoiceProperties#getFileNamingMethod()} 返回的枚举值，
   * 创建对应的命名规则实现：</p>
   * <ul>
   *   <li>{@code DATE} - {@link DateNamingRulesGenerationService} 按日期时间命名</li>
   *   <li>{@code UUID} - {@link UUIDNamingRulesGenerationService} 按 UUID 命名</li>
   *   <li>{@code SNOWFLAKE} - {@link SnowflakeNamingRulesGenerationService} 按雪花 ID 命名</li>
   *   <li>其他情况（含 {@code ORIGINAL}）- {@link OriginalNamingRulesGenerationService} 保留原名</li>
   * </ul>
   *
   * @param properties 文件存储配置属性
   * @return 文件命名规则服务实例
   */
  @Bean
  @ConditionalOnMissingBean(FileNamingRulesGenerationService.class)
  public FileNamingRulesGenerationService fileNamingRule(FileChoiceProperties properties) {
    logger.info("选择 {} 文件命名规则", properties.getFileNamingMethod().getDesc());
    switch (properties.getFileNamingMethod()) {
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

  /**
   * 未选择具体供应商时提供默认服务，调用时会抛出明确的无实现异常。
   *
   * <p>当配置项 {@code file.choice} 未设置或设置为 {@code DEFAULT} 时生效，
   * 确保在没有引入任何供应商模块时不会静默失败。</p>
   *
   * @param properties      文件存储配置属性
   * @param fileNamingRule  文件命名规则
   * @param clamAvUtils     ClamAV 病毒扫描工具
   * @return 默认文件服务实例（所有操作均抛出无实现异常）
   */
  @Bean
  @ConditionalOnMissingBean(FileServiceInterface.class)
  @ConditionalOnProperty(prefix = "file", name = "choice", havingValue = "DEFAULT", matchIfMissing = true)
  public FileServiceInterface defaultFileService(
      FileChoiceProperties properties,
      FileNamingRulesGenerationService fileNamingRule,
      ClamAvUtils clamAvUtils) {
    return new DefaultService(properties, fileNamingRule, clamAvUtils);
  }
}
