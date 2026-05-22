package com.steven.solomon.base.config;

import com.steven.solomon.base.profile.SwaggerProfile;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

/**
 * Swagger/OpenAPI自动配置。
 *
 * <p>基于Spring Boot 3和Springdoc OpenAPI实现，替代旧版Springfox配置。
 * 业务模块只需要在配置文件中设置 {@code solomon.swagger.*} 和 {@code springdoc.*}
 * 即可启用接口文档。</p>
 */
@AutoConfiguration
@ConditionalOnClass(OpenAPI.class)
@EnableConfigurationProperties(SwaggerProfile.class)
@ConditionalOnProperty(prefix = "solomon.swagger", name = "enabled", havingValue = "true",
    matchIfMissing = true)
public class SwaggerConfig {

  /**
   * 创建OpenAPI基础信息。
   *
   * @param profile Swagger配置属性，包含标题、版本和描述
   * @return OpenAPI基础信息Bean
   */
  @Bean
  @ConditionalOnMissingBean
  public OpenAPI solomonOpenApi(SwaggerProfile profile) {
    return new OpenAPI().info(new Info()
        .title(profile.getTitle())
        .version(profile.getVersion())
        .description(profile.getDescription()));
  }

  /**
   * 创建全局请求参数自定义器。
   *
   * <p>会把 {@code solomon.swagger.global-request-parameters} 中配置的参数追加到所有接口。
   * 常见用途是添加token、tenantCode等Header。</p>
   *
   * @param profile Swagger配置属性
   * @return OpenAPI自定义器
   */
  @Bean
  @ConditionalOnMissingBean(name = "solomonOpenApiCustomizer")
  public OpenApiCustomizer solomonOpenApiCustomizer(SwaggerProfile profile) {
    return openApi -> {
      if (openApi.getPaths() == null || profile.getGlobalRequestParameters() == null) {
        return;
      }
      openApi.getPaths().values().forEach(pathItem ->
          pathItem.readOperations().forEach(operation ->
              profile.getGlobalRequestParameters().stream()
                  .filter(parameter -> parameter != null
                      && StringUtils.hasText(parameter.getName())
                      && !parameter.isHidden())
                  .forEach(parameter -> operation.addParametersItem(new Parameter()
                      .name(parameter.getName())
                      .in(StringUtils.hasText(parameter.getIn())
                          ? parameter.getIn().toLowerCase()
                          : "header")
                      .description(parameter.getDescription())
                      .required(parameter.isRequired())
                      .schema(new StringSchema())))));
    };
  }
}
