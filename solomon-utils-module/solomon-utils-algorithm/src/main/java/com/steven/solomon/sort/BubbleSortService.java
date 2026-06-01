package com.steven.solomon.sort;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 冒泡排序实现。
 *
 * <p>每一轮把当前最大元素移动到尾部；如果某一轮没有发生交换，说明列表已经有序，
 * 可以提前结束，减少不必要的比较。</p>
 */
public class BubbleSortService implements SortService {

  @Override
  public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {
    List<T> sortedList = copyToList(list);
    boolean swapped;
    int boundary = sortedList.size();

    do {
      swapped = false;
      for (int i = 1; i < boundary; i++) {
        if (comparator.compare(sortedList.get(i - 1), sortedList.get(i)) > 0) {
          swap(sortedList, i - 1, i);
          swapped = true;
        }
      }
      boundary--;
    } while (swapped);
    return sortedList;
  }
}
