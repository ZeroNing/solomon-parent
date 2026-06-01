package com.steven.solomon.sort;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 梳排序实现。
 *
 * <p>梳排序通过较大的 gap 先消除远距离逆序，再逐渐退化为冒泡排序。</p>
 */
public class CombSortService implements SortService {

  private static final double SHRINK_FACTOR = 1.3D;

  @Override
  public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {
    List<T> sortedList = copyToList(list);
    int gap = sortedList.size();
    boolean swapped = false;

    while (gap > 1 || swapped) {
      gap = nextGap(gap);
      swapped = compareAndSwap(sortedList, gap, comparator);
    }
    return sortedList;
  }

  private int nextGap(int gap) {
    return gap > 1 ? Math.max(1, (int) (gap / SHRINK_FACTOR)) : 1;
  }

  /**
   * 按指定 gap 比较并交换逆序元素。
   */
  private <T> boolean compareAndSwap(List<T> list, int gap, Comparator<? super T> comparator) {
    boolean swapped = false;
    for (int i = 0; i + gap < list.size(); i++) {
      if (comparator.compare(list.get(i), list.get(i + gap)) > 0) {
        swap(list, i, i + gap);
        swapped = true;
      }
    }
    return swapped;
  }
}
