package com.steven.solomon.config.i18n;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class ResourceBundleEnumeration implements Enumeration<String> {

  Set<String>         set;
  Iterator<String>    iterator;
  Enumeration<String> enumeration; // may remain null

  /**
   * Constructs a resource bundle enumeration.
   * @param set an set providing some elements of the enumeration
   * @param enumeration an enumeration providing more elements of the enumeration.
   *        enumeration may be null.
   */
  public ResourceBundleEnumeration(Set<String> set, Enumeration<String> enumeration) {
    this.set = set;
    this.iterator = set.iterator();
    this.enumeration = enumeration;
  }

  String next = null;


  @Override
  public boolean hasMoreElements() {
    if (next == null) {
      if (iterator.hasNext()) {
        next = iterator.next();
      } else if (enumeration != null) {
        while (next == null && enumeration.hasMoreElements()) {
          next = enumeration.nextElement();
          if (set.contains(next)) {
            next = null;
          }
        }
      }
    }
    return next != null;
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
