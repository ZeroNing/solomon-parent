package com.steven.solomon.base.config;

import com.steven.solomon.base.profile.SwaggerProfile;
import com.steven.solomon.verification.ValidateUtils;
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

/**
 * Solomon 的 OpenAPI 自动配置。
 *
 * <p>这里不负责扫描接口包，也不负责 Swagger UI / Knife4j 页面路径，这些仍然交给
 * {@code springdoc.*} 和 {@code knife4j.*}。本类只做两件事：</p>
 *
 * <ul>
 *   <li>提供默认的 OpenAPI 基础信息，例如标题、版本、描述。</li>
 *   <li>把业务配置的全局请求参数追加到每个接口上，例如 token、tenantCode。</li>
 * </ul>
 *
 * <p>所有 Bean 都允许业务项目覆盖，starter 不强行接管最终行为。</p>
 */
@AutoConfiguration
@ConditionalOnClass(OpenAPI.class)
@EnableConfigurationProperties(SwaggerProfile.class)
@ConditionalOnProperty(prefix = "solomon.swagger", name = "enabled", havingValue = "true",
    matchIfMissing = true)
public class SwaggerConfig {

  /**
   * OpenAPI 文档基础信息。
   *
   * @param profile Solomon 文档配置
   * @return OpenAPI 根对象
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
   * 为所有接口追加全局请求参数。
   *
   * <p>隐藏参数不会写入 OpenAPI，空名称参数会被跳过，避免生成不可用的接口文档。</p>
   *
   * @param profile Solomon 文档配置
   * @return Springdoc 自定义器
   */
  @Bean
  @ConditionalOnMissingBean(name = "solomonOpenApiCustomizer")
  public OpenApiCustomizer solomonOpenApiCustomizer(SwaggerProfile profile) {
    return openApi -> {
      if (ValidateUtils.isEmpty(openApi.getPaths())
          || ValidateUtils.isEmpty(profile.getGlobalRequestParameters())) {
        return;
      }
      openApi.getPaths().values().forEach(pathItem ->
          pathItem.readOperations().forEach(operation ->
              profile.getGlobalRequestParameters().stream()
                  .filter(parameter -> ValidateUtils.isNotEmpty(parameter)
                      && ValidateUtils.isNotEmpty(parameter.getName())
                      && !parameter.isHidden())
                  .map(this::toOpenApiParameter)
                  .forEach(operation::addParametersItem)));
    };
  }

  /**
   * 将文档配置中的参数模型转换为 OpenAPI Parameter 对象。
   *
   * @param parameter 文档配置中的请求参数模型
   * @return OpenAPI Parameter 对象
   */
  private Parameter toOpenApiParameter(SwaggerProfile.DocRequestParameter parameter) {
    String position = ValidateUtils.isNotEmpty(parameter.getIn())
        ? parameter.getIn().toLowerCase()
        : "header";
    return new Parameter()
        .name(parameter.getName())
        .in(position)
        .description(parameter.getDescription())
        .required(parameter.isRequired())
        .schema(new StringSchema());
  }
}
