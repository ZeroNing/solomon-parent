package com.steven.solomon.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class I18nControl extends ResourceBundle.Control{
  private static final String ALL_CLASSPATH_URL_PERFIX = "classpath*:";
  private static final String PROPERTY_ENCODING = "UTF-8";

  @Override
  public ResourceBundle newBundle(String baseName, Locale locale, String format,
      ClassLoader classLoader, boolean reload)
      throws IllegalAccessException, InstantiationException, IOException {
    // 例如将classpath*:i18n/messages_zh.properties全放到一个集合中
    String bundleName = super.toBundleName(baseName, locale);
    final String               resourceName = bundleName + ".properties";
    I18nPropertyResourceBundle bundle       = null;
    if ("java.class".equals(format)) {
      // 不支持
      bundle = null;
    } else if ("java.properties".equals(format)) {
      if (bundleName.startsWith(ALL_CLASSPATH_URL_PERFIX)) {
        bundle = this.getBundleFromAllClasspath(resourceName, classLoader, reload);
      } else {
        bundle = this.getBundleFromClasspath(resourceName, classLoader, reload);
      }
    }
    return bundle;
  }

  private I18nPropertyResourceBundle getBundleFromAllClasspath(String resourceName,
      ClassLoader classLoader,
      boolean reload) throws IOException {
    resourceName = resourceName.substring(ALL_CLASSPATH_URL_PERFIX.length(), resourceName.length());
    Enumeration<URL> enumeration = classLoader.getResources(resourceName);
    Map<String, URL> urlMap      = new HashMap<>(16);
    URL              tempURL;
    while (enumeration.hasMoreElements()) {
      tempURL = enumeration.nextElement();
      urlMap.put(tempURL.toString(), tempURL);
    }

    if (urlMap.isEmpty()) {
      return null;
    }

    I18nPropertyResourceBundle bundle = new I18nPropertyResourceBundle();
    for (URL url : urlMap.values()) {
      bundle.combine(this.propertyFromURL(url, reload));
    }

    return bundle;
  }

  private I18nPropertyResourceBundle getBundleFromClasspath(String resourceName,
      ClassLoader classLoader,
      final boolean reload) throws IOException {
    I18nPropertyResourceBundle bundle = null;
    InputStream                stream = null;
    try {
      stream = AccessController.doPrivileged(
          new PrivilegedExceptionAction<InputStream>() {
            @Override
            public InputStream run() throws IOException {
              InputStream is = null;
              if (reload) {
                URL url = classLoader.getResource(resourceName);
                if (url != null) {
                  URLConnection connection = url.openConnection();
                  if (connection != null) {
                    // Disable caches to get fresh data for
                    // reloading.
                    connection.setUseCaches(false);
                    is = connection.getInputStream();
                  }
                }
              } else {
                is = classLoader.getResourceAsStream(resourceName);
              }
              return is;
            }
          });
    } catch (PrivilegedActionException e) {
      throw (IOException) e.getException();
    }
    if (stream != null) {
      try {
        bundle = new I18nPropertyResourceBundle(new InputStreamReader(stream, PROPERTY_ENCODING));
      } finally {
        stream.close();
      }
    }
    return bundle;
  }

  private I18nPropertyResourceBundle propertyFromURL(final URL url, final boolean reload) throws IOException {
    I18nPropertyResourceBundle bundle = null;
    InputStream                stream = null;
    try {
      stream = AccessController.doPrivileged(
          new PrivilegedExceptionAction<InputStream>() {
            @Override
            public InputStream run() throws IOException {
              InputStream is = null;
              if (reload) {
                URLConnection connection = url.openConnection();
                if (connection != null) {
                  // Disable caches to get fresh data for
                  // reloading.
                  connection.setUseCaches(false);
                  is = connection.getInputStream();
                }
              } else {
                is = url.openStream();
              }
              return is;
            }
          });
    } catch (PrivilegedActionException e) {
      throw (IOException) e.getException();
    }
    if (stream != null) {
      try {
        bundle = new I18nPropertyResourceBundle(new InputStreamReader(stream, PROPERTY_ENCODING));
      } finally {
        stream.close();
      }
    }

    return bundle;
  }

}
