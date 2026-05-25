package com.steven.solomon.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.steven.solomon.json.config.JsonConfig;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 基础配置。
 *
 * <p>该配置只做通用、低侵入的 Web 增强：</p>
 * <ul>
 *   <li>统一 StringHttpMessageConverter 的默认编码为 UTF-8。</li>
 *   <li>让已有 Jackson 转换器复用 Solomon 的 ObjectMapper。</li>
 *   <li>使用 AntPathMatcher，兼容旧项目中的 Ant 风格路径匹配。</li>
 * </ul>
 *
 * <p>不再使用 {@code @ConditionalOnMissingBean(WebMvcConfigurer.class)}，
 * 避免业务项目只要声明自己的 WebMvcConfigurer，就导致 Solomon 的基础配置整体失效。</p>
 */
@AutoConfiguration
@Import(JsonConfig.class)
@ConditionalOnProperty(prefix = "solomon.web", name = "enabled", havingValue = "true",
    matchIfMissing = true)
public class WebConfig implements WebMvcConfigurer {

  private final ObjectMapper mapper;

  public WebConfig(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  /**
   * 扩展 Spring MVC 消息转换器。
   */
  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.stream()
        .filter(StringHttpMessageConverter.class::isInstance)
        .map(StringHttpMessageConverter.class::cast)
        .forEach(converter -> converter.setDefaultCharset(StandardCharsets.UTF_8));

    converters.stream()
        .filter(MappingJackson2HttpMessageConverter.class::isInstance)
        .map(MappingJackson2HttpMessageConverter.class::cast)
        .forEach(converter -> converter.setObjectMapper(mapper));
  }

  /**
   * 配置路径匹配策略。
   */
  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    configurer.setPathMatcher(new AntPathMatcher());
  }
}
