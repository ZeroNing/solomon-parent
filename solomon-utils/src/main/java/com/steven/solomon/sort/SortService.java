package com.steven.solomon.sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 排序算法统一接口。
 *
 * <p>各排序实现只关注自己的算法步骤，公共的集合复制、元素交换放在默认方法中。
 * 这样既避免重复代码，也能统一保证“不直接修改调用方传入集合”的约定。</p>
 */
public interface SortService {

  /**
   * 对给定集合排序。
   *
   * @param list 原始集合
   * @param comparator 元素比较器
   * @param <T> 元素类型
   * @return 排序后的新集合
   */
  <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator);

  /**
   * 复制为可随机访问的列表。
   *
   * <p>排序算法通常需要按下标读取和交换元素，因此统一转换为 {@link ArrayList}。
   * 返回新集合可以避免排序过程修改调用方传入的原始集合。</p>
   */
  default <T> List<T> copyToList(Collection<T> source) {
    return new ArrayList<>(source);
  }

  /**
   * 交换列表中两个元素的位置。
   */
  default <T> void swap(List<T> list, int left, int right) {
    if (left == right) {
      return;
    }
    T temp = list.get(left);
    list.set(left, list.get(right));
    list.set(right, temp);
  }
}
