package com.steven.solomon.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.steven.solomon.condition.JsonCondition;
import com.steven.solomon.json.config.JsonConfig;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Import(value = {JsonConfig.class})
@ConditionalOnMissingBean(WebMvcConfigurer.class)
public class WebConfig implements WebMvcConfigurer {

  private final ObjectMapper mapper;

  public WebConfig(ObjectMapper mapper) {this.mapper = mapper;}

  @Override
  public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.stream()
        // 过滤出StringHttpMessageConverter类型实例
        .filter(StringHttpMessageConverter.class::isInstance)
        .map(c -> (StringHttpMessageConverter) c)
        // 这里将转换器的默认编码设置为utf-8
        .forEach(c -> c.setDefaultCharset(StandardCharsets.UTF_8));
    //创建消息转换器对象
    MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
    //设置对象转换器，底层使用Jackson将Java对象转为json
    messageConverter.setObjectMapper(mapper);
    //将上面的消息转换器对象追加到mvc框架的转换器集合中
    converters.add(0, messageConverter);
  }

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    configurer.setPathMatcher(new AntPathMatcher());
  }
}
