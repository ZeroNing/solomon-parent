package com.steven.solomon.config.i18n;

import com.steven.solomon.code.BaseCode;
import cn.hutool.core.util.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 国际化配置。
 *
 * <p>支持显式配置 i18n 文件路径，也支持扫描 classpath 下所有 {@code i18n/messages*.properties}。</p>
 */
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
   * 初始化 I18N 国际化文件。
   */
  @Bean("messageSource")
  @ConditionalOnMissingBean(MessageSource.class)
  public MessageSource init() {
    List<String> allPath = buildBaseNames();
    List<String> localeList = splitConfig(allLocale);
    List<String> beanNames = new ArrayList<>();
    for (String basePath : allPath) {
      ResourceBundle resourceBundle = initResources(localeList, basePath);
      if (ObjectUtil.isNotEmpty(resourceBundle)) {
        beanNames.add(resourceBundle.getBaseBundleName());
      }
    }

    ResourceBundleMessageSource bundleMessageSource = new ResourceBundleMessageSource();
    bundleMessageSource.setDefaultEncoding(BaseCode.UTF8);
    bundleMessageSource.setBasenames(beanNames.toArray(new String[0]));
    Locale effectiveLocale = ObjectUtil.defaultIfNull(defaultLocale, Locale.CHINESE);
    bundleMessageSource.setDefaultLocale(effectiveLocale);
    logger.info("I18nConfig初始化I18N国际化文件成功, 默认语言={}, 文件路径={}", effectiveLocale, beanNames);
    return bundleMessageSource;
  }

  private List<String> buildBaseNames() {
    List<String> allPath = new ArrayList<>();
    allPath.addAll(splitConfig(path));
    if (isScanClass) {
      allPath.add("classpath*:i18n/messages");
    }
    return allPath;
  }

  private List<String> splitConfig(String value) {
    if (ObjectUtil.isEmpty(value)) {
      return new ArrayList<>();
    }
    return new ArrayList<>(Arrays.asList(value.split(",")));
  }

  private ResourceBundle initResources(List<String> locales, String basePath) {
    ResourceBundle resourceBundle = null;
    for (String language : locales) {
      if (ObjectUtil.isEmpty(language)) {
        continue;
      }
      resourceBundle = ResourceBundle.getBundle(basePath, new Locale(language), new I18nControl());
    }
    return resourceBundle;
  }
}
