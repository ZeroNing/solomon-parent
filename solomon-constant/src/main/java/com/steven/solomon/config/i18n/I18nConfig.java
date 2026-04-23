package com.steven.solomon.config.i18n;

import com.steven.solomon.code.BaseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.*;

@Configuration(proxyBeanMethods = false)
public class I18nConfig {

  private static final Logger logger = LoggerFactory.getLogger(I18nConfig.class);

  @Value("${i18n.language:zh}")
  private Locale defaultLocale;

  @Value("${i18n.all-locale:zh}")
  private String allLocale;

  @Value("${i18n.path:}")
  private String path;

  @Value("${i18n.is-scan-class:true}")
  private boolean isScanClass;

  /**
   * 初始化I18N国际化文件
   */
  @Bean("messageSource")
  @ConditionalOnMissingBean(MessageSource.class)
  public MessageSource init() {
    List<String> allPath = new ArrayList<>();
    List<String> localeList = Arrays.asList(allLocale.split(","));
    if (!path.isEmpty()) {
      allPath.addAll(Arrays.asList(path.split(",")));
    }
    if (isScanClass) {
      allPath.add("classpath*:i18n/messages");
    }
    List<String> beanNames = new ArrayList<>();
    for (String p : allPath) {
      ResourceBundle resourceBundle = initResources(localeList, 0, null, p);
      beanNames.add(resourceBundle.getBaseBundleName());
    }

    ResourceBundleMessageSource bundleMessageSource = new ResourceBundleMessageSource();
    bundleMessageSource.setDefaultEncoding(BaseCode.UTF8);
    bundleMessageSource.setBasenames(beanNames.toArray(new String[]{}));
    Locale effectiveLocale = defaultLocale == null ? Locale.CHINESE : defaultLocale;
    bundleMessageSource.setDefaultLocale(effectiveLocale);
    logger.info("I18nConfig初始化I18N国际化文件成功, 默认语言={}, 文件路径={}", effectiveLocale, beanNames);
    return bundleMessageSource;
  }

  private ResourceBundle initResources(List<String> locales, int index, ResourceBundle resourceBundle, String basePath) {
    if (index >= locales.size()) {
      return resourceBundle;
    }
    String language = locales.get(index);
    if (language == null || language.isEmpty()) {
      return resourceBundle;
    }
    resourceBundle = ResourceBundle.getBundle(basePath, new Locale(language), new I18nControl());
    return initResources(locales, index + 1, resourceBundle, basePath);
  }
}
