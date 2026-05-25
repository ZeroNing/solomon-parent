package com.steven.solomon.config.i18n;


import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * 支持合并的国际化资源 Bundle。
 */
public class I18nPropertyResourceBundle extends ResourceBundle {

  private final Map<String,Object> lookup;

  public I18nPropertyResourceBundle() {
    lookup = new HashMap<>(16);
  }

  public I18nPropertyResourceBundle(InputStream stream) throws IOException {
    lookup = loadProperties(stream);
  }

  public I18nPropertyResourceBundle(Reader reader) throws IOException {
    Properties properties = new Properties();
    properties.load(reader);
    lookup = toLookup(properties);
  }

  @Override
  public Object handleGetObject(String key) {
    if (key.isEmpty()) {
      throw new NullPointerException();
    }
    return lookup.get(key);
  }

  @Override
  public Enumeration<String> getKeys() {
    ResourceBundle parent = this.parent;
    return new ResourceBundleEnumeration(lookup.keySet(),
        (parent != null) ? parent.getKeys() : null);
  }

  @Override
  protected Set<String> handleKeySet() {
    return Collections.unmodifiableSet(lookup.keySet());
  }

  /**
   * 合并其他 Bundle，已有 key 优先保留，避免后加载资源覆盖先加载资源。
   */
  public void combine(I18nPropertyResourceBundle other) {
    if (other != null) {
      other.lookup.forEach(lookup::putIfAbsent);
    }
  }

  public boolean isEmpty() {
    return lookup.isEmpty();
  }

  private Map<String, Object> loadProperties(InputStream stream) throws IOException {
    Properties properties = new Properties();
    properties.load(stream);
    return toLookup(properties);
  }

  private Map<String, Object> toLookup(Properties properties) {
    Map<String, Object> result = new HashMap<>(properties.size());
    properties.forEach((key, value) -> result.put(String.valueOf(key), value));
    return result;
  }

}
