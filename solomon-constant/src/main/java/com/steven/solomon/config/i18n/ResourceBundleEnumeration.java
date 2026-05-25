package com.steven.solomon.config.i18n;

import com.steven.solomon.verification.ValidateUtils;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class ResourceBundleEnumeration implements Enumeration<String> {

  private final Set<String> set;
  private final Iterator<String> iterator;
  private final Enumeration<String> enumeration;
  private String next;

  /**
   * 创建资源 key 枚举器。
   *
   * @param set 当前 Bundle 的 key 集合
   * @param enumeration 父 Bundle 的 key 枚举，可为空
   */
  public ResourceBundleEnumeration(Set<String> set, Enumeration<String> enumeration) {
    this.set = set;
    this.iterator = set.iterator();
    this.enumeration = enumeration;
  }


  @Override
  public boolean hasMoreElements() {
    if (ValidateUtils.isEmpty(next)) {
      if (iterator.hasNext()) {
        next = iterator.next();
      } else if (ValidateUtils.isNotEmpty(enumeration)) {
        while (ValidateUtils.isEmpty(next) && enumeration.hasMoreElements()) {
          next = enumeration.nextElement();
          if (set.contains(next)) {
            next = null;
          }
        }
      }
    }
    return ValidateUtils.isNotEmpty(next);
  }

  @Override
  public String nextElement() {
    if (hasMoreElements()) {
      String result = next;
      next = null;
      return result;
    } else {
      throw new NoSuchElementException();
    }
  }

}
