package com.steven.solomon.sort;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 希尔排序实现。
 *
 * <p>希尔排序是插入排序的分组优化版，通过逐步缩小 gap 让元素更快接近最终位置。</p>
 */
public class ShellSortService implements SortService {

  @Override
  public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {
    List<T> sortedList = copyToList(list);
    for (int gap = sortedList.size() / 2; gap > 0; gap /= 2) {
      gapInsertionSort(sortedList, gap, comparator);
    }
    return sortedList;
  }

  /**
   * 按指定间隔执行插入排序。
   */
  private <T> void gapInsertionSort(List<T> list, int gap, Comparator<? super T> comparator) {
    for (int i = gap; i < list.size(); i++) {
      T current = list.get(i);
      int j = i;
      while (j >= gap && comparator.compare(list.get(j - gap), current) > 0) {
        list.set(j, list.get(j - gap));
        j -= gap;
      }
      list.set(j, current);
    }
  }
}
