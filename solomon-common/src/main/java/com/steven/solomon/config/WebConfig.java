package com.steven.solomon.config;

import com.steven.solomon.header.RequestHeaderHolder;
import com.steven.solomon.json.config.JacksonObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
@Import(value = {JacksonObjectMapper.class})
public class WebConfig implements WebMvcConfigurer {

  private final JacksonObjectMapper jacksonObjectMapper;

  public WebConfig(JacksonObjectMapper jacksonObjectMapper) {this.jacksonObjectMapper = jacksonObjectMapper;}

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
    messageConverter.setObjectMapper(jacksonObjectMapper);
    //将上面的消息转换器对象追加到mvc框架的转换器集合中
    converters.add(0, messageConverter);
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new Converter<String, LocalDateTime>() {

      private LocalDateTime convertLocalDateTime(LocalDateTime localDateTime, ZoneId originZoneId,
          ZoneId targetZoneId) {
        return localDateTime.atZone(originZoneId).withZoneSameInstant(targetZoneId).toLocalDateTime();
      }

      @Override
      public LocalDateTime convert(String source) {
        return convertLocalDateTime(
            LocalDateTime.parse(source, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")),
            ZoneId.of(RequestHeaderHolder.getServerTimeZone()), ZoneId.systemDefault());
      }
    });
  }
}
