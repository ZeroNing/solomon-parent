package com.steven.solomon.gateway.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 网关国际化配置。
 */
@ConfigurationProperties("gateway.i18n")
public class GatewayI18nProperties {

  /**
   * 请求未指定语言时使用的默认语言。
   */
  private Locale defaultLocale = Locale.CHINESE;

  /**
   * 网关允许使用的语言。
   */
  private List<Locale> supportedLocales = new ArrayList<>(List.of(Locale.CHINESE, Locale.ENGLISH));

  public Locale getDefaultLocale() {
    return defaultLocale;
  }

  public void setDefaultLocale(Locale defaultLocale) {
    this.defaultLocale = defaultLocale;
  }

  public List<Locale> getSupportedLocales() {
    return supportedLocales;
  }

  public void setSupportedLocales(List<Locale> supportedLocales) {
    this.supportedLocales = supportedLocales;
  }
}
