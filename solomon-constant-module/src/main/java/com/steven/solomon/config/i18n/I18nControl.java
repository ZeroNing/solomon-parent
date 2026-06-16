package com.steven.solomon.config.i18n;

import cn.hutool.core.util.ObjectUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 国际化资源加载控制器。
 *
 * <p>增强 JDK ResourceBundle：支持 {@code classpath*:} 多位置合并，并统一用 UTF-8 读取 properties。</p>
 */
public class I18nControl extends ResourceBundle.Control {

  private static final String ALL_CLASSPATH_URL_PREFIX = "classpath*:";
  private static final String JAVA_PROPERTIES_FORMAT = "java.properties";
  private static final Map<URL, Long> LAST_MODIFIED_CACHE = new ConcurrentHashMap<>();

  @Override
  public ResourceBundle newBundle(String baseName, Locale locale, String format,
      ClassLoader classLoader, boolean reload) throws IOException {
    if (!JAVA_PROPERTIES_FORMAT.equals(format)) {
      return null;
    }
    String bundleName = toBundleName(baseName, locale);
    String resourceName = bundleName + ".properties";
    return bundleName.startsWith(ALL_CLASSPATH_URL_PREFIX)
        ? getBundleFromAllClasspath(resourceName, classLoader, reload)
        : getBundleFromClasspath(resourceName, classLoader, reload);
  }

  private I18nPropertyResourceBundle getBundleFromAllClasspath(String resourceName,
      ClassLoader classLoader, boolean reload) throws IOException {
    String actualName = resourceName.substring(ALL_CLASSPATH_URL_PREFIX.length());
    Enumeration<URL> urls = classLoader.getResources(actualName);
    I18nPropertyResourceBundle combinedBundle = new I18nPropertyResourceBundle();
    while (urls.hasMoreElements()) {
      URL url = urls.nextElement();
      try (InputStream stream = openStreamWithReload(url, reload)) {
        if (ObjectUtil.isNotEmpty(stream)) {
          combinedBundle.combine(newBundle(stream));
        }
      }
    }
    return combinedBundle.isEmpty() ? null : combinedBundle;
  }

  private I18nPropertyResourceBundle getBundleFromClasspath(String resourceName,
      ClassLoader classLoader, boolean reload) throws IOException {
    URL url = classLoader.getResource(resourceName);
    if (ObjectUtil.isEmpty(url)) {
      return null;
    }
    try (InputStream stream = openStreamWithReload(url, reload)) {
      return ObjectUtil.isEmpty(stream) ? null : newBundle(stream);
    }
  }

  /**
   * 按 reload 标记打开资源流；资源未变化时返回 null，避免重复加载。
   */
  private InputStream openStreamWithReload(URL url, boolean reload) throws IOException {
    if (!reload) {
      return url.openStream();
    }
    URLConnection connection = url.openConnection();
    if (connection instanceof HttpURLConnection httpConnection) {
      httpConnection.setRequestProperty("Cache-Control", "no-cache");
    }
    long lastModified = connection.getLastModified();
    if (LAST_MODIFIED_CACHE.containsKey(url)
        && LAST_MODIFIED_CACHE.get(url) == lastModified) {
      return null;
    }
    LAST_MODIFIED_CACHE.put(url, lastModified);
    connection.setUseCaches(false);
    return connection.getInputStream();
  }

  private I18nPropertyResourceBundle newBundle(InputStream stream) throws IOException {
    return new I18nPropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8));
  }
}
