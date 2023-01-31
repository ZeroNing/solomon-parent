package com.steven.solomon.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import sun.util.ResourceBundleEnumeration;

public class I18nPropertyResourceBundle extends ResourceBundle {

  // ==================privates====================
  private Map<String,Object> lookup;

  // 添加额外构造函数，用于合并多个bundle对象
  public I18nPropertyResourceBundle() {
    lookup = new HashMap<>(16);
  }

  public I18nPropertyResourceBundle(InputStream stream) throws IOException {
    Properties properties = new Properties();
    properties.load(stream);
    lookup = new HashMap(properties);
  }

  public I18nPropertyResourceBundle(Reader reader) throws IOException {
    Properties properties = new Properties();
    properties.load(reader);
    lookup = new HashMap(properties);
  }

  @Override
  public Object handleGetObject(String key) {
    if (key.isEmpty() || "".equals(key)) {
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
    return lookup.keySet();
  }

  // 合并其他bundle对象的数据
  public void combine(I18nPropertyResourceBundle others) {
    if (others != null) {
      lookup.putAll(others.lookup);
    }
  }

}
