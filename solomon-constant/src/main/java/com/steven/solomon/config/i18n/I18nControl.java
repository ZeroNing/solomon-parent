package com.steven.solomon.config.i18n;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class I18nControl extends ResourceBundle.Control{
  private static final String ALL_CLASSPATH_URL_PREFIX = "classpath*:";
  private static final Map<URL, Long> LAST_MODIFIED_CACHE = new ConcurrentHashMap<>();

  @Override
  public ResourceBundle newBundle(String baseName, Locale locale, String format,
      ClassLoader classLoader, boolean reload)
      throws IOException {
    String bundleName = toBundleName(baseName, locale);
    String resourceName = bundleName + ".properties";

    if ("java.properties".equals(format)) {
      if (bundleName.startsWith(ALL_CLASSPATH_URL_PREFIX)) {
        return getBundleFromAllClasspath(resourceName, classLoader, reload);
      } else {
        return getBundleFromClasspath(resourceName, classLoader, reload);
      }
    }
    return null;
  }

  private I18nPropertyResourceBundle getBundleFromAllClasspath(String resourceName,
      ClassLoader classLoader,
      boolean reload) throws IOException {
    String actualName = resourceName.substring(ALL_CLASSPATH_URL_PREFIX.length());
    Enumeration<URL> urls = classLoader.getResources(actualName);

    I18nPropertyResourceBundle combinedBundle = new I18nPropertyResourceBundle();
    while (urls.hasMoreElements()) {
      URL url = urls.nextElement();
      try (InputStream stream = openStreamWithReload(url, reload)) {
        if (stream != null) {
          combinedBundle.combine(new I18nPropertyResourceBundle(
              new InputStreamReader(stream, StandardCharsets.UTF_8)));
        }
      }
    }
    return combinedBundle.isEmpty() ? null : combinedBundle;
  }

  private I18nPropertyResourceBundle getBundleFromClasspath(String resourceName,
      ClassLoader classLoader,
      boolean reload) throws IOException {
    URL url = classLoader.getResource(resourceName);
    if (url == null) return null;

    try (InputStream stream = openStreamWithReload(url, reload)) {
      return stream != null
             ? new I18nPropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8))
             : null;
    }
  }

  /**
   * 带缓存控制的流打开方法
   */
  private InputStream openStreamWithReload(URL url, boolean reload) throws IOException {
    if (!reload) return url.openStream();

    URLConnection connection = url.openConnection();
    if (connection instanceof HttpURLConnection httpConn) {
      httpConn.setRequestProperty("Cache-Control", "no-cache");
    }

    // 基于最后修改时间的缓存验证
    long lastModified = connection.getLastModified();
    if (LAST_MODIFIED_CACHE.containsKey(url) &&
        LAST_MODIFIED_CACHE.get(url) == lastModified) {
      return null; // 未修改时跳过加载
    }

    LAST_MODIFIED_CACHE.put(url, lastModified);
    connection.setUseCaches(false);
    return connection.getInputStream();
  }

  // 假设 I18nPropertyResourceBundle 实现以下方法
  private static class I18nPropertyResourceBundle extends ResourceBundle {
    private final Properties properties = new Properties();

    public I18nPropertyResourceBundle() {}

    public I18nPropertyResourceBundle(Reader reader) throws IOException {
      try (BufferedReader br = new BufferedReader(reader)) {
        properties.load(br);
      }
    }

    public void combine(I18nPropertyResourceBundle other) {
      other.properties.forEach((k, v) -> properties.putIfAbsent(k, v));
    }

    public boolean isEmpty() {
      return properties.isEmpty();
    }

    @Override
    protected Object handleGetObject(String key) {
      return properties.get(key);
    }

    @Override
    public Enumeration<String> getKeys() {
      return Collections.enumeration(properties.stringPropertyNames());
    }
  }

}
