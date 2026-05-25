package com.steven.solomon.sort;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Library Sort 的简化实现。
 *
 * <p>这里使用 gap 插入排序加最终插入排序的方式模拟 Library Sort 的分散插入思想。
 * 公共复制逻辑交给 {@link SortService#copyToList(Collection)}，避免重复代码。</p>
 */
public class LibrarySortService implements SortService {

  @Override
  public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {
    List<T> sortedList = copyToList(list);
    int gap = (int) Math.sqrt(sortedList.size());

    gapInsertionSort(sortedList, Math.max(1, gap), comparator);
    gapInsertionSort(sortedList, 1, comparator);
    return sortedList;
  }

  /**
   * 按指定间隔执行插入排序；gap 为 1 时就是标准插入排序。
   */
  private <T> void gapInsertionSort(List<T> list, int gap, Comparator<? super T> comparator) {
    for (int i = gap; i < list.size(); i++) {
      T key = list.get(i);
      int j = i;
      while (j >= gap && comparator.compare(list.get(j - gap), key) > 0) {
        list.set(j, list.get(j - gap));
        j -= gap;
      }
      list.set(j, key);
    }
  }
}
