package com.steven.solomon.config;

import com.steven.solomon.service.EpcService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * EPC 编解码自动配置类。
 * <p>
 * 当 Spring 容器中不存在 {@link EpcService} Bean 时，
 * 自动注册一个默认的 EPC 编解码服务实例。
 * </p>
 *
 * @author 创建者
 */
@Configuration(proxyBeanMethods = false)
public class EpcAutoConfiguration {

  /**
   * 注册默认的 EPC 编解码服务 Bean。
   * <p>
   * 仅在容器中不存在 {@link EpcService} 类型的 Bean 时生效。
   * </p>
   *
   * @return 默认的 {@link EpcService} 实例
   */
  @Bean
  @ConditionalOnMissingBean
  public EpcService epcService() {
    return new EpcService();
  }
}
