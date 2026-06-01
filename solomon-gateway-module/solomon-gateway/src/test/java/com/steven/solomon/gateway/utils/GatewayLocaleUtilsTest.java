package com.steven.solomon.gateway.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.steven.solomon.gateway.properties.GatewayI18nProperties;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class GatewayLocaleUtilsTest {

  @Test
  void selectSupportedLanguageAndFallbackToDefault() {
    GatewayLocaleUtils localeUtils = new GatewayLocaleUtils(new GatewayI18nProperties());

    assertEquals(Locale.ENGLISH, localeUtils.getLocale("en-US,en;q=0.9"));
    assertEquals(Locale.CHINESE, localeUtils.getLocale("fr-FR"));
    assertEquals(Locale.CHINESE, localeUtils.getLocale(""));
  }
}
