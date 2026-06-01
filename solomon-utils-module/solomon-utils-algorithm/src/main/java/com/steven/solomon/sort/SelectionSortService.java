package com.steven.solomon.sort;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 选择排序实现。
 *
 * <p>每一轮从未排序区间选择最小元素，放到当前轮次的起始位置。
 * 算法简单稳定性较差，适合小集合或教学场景。</p>
 */
public class SelectionSortService implements SortService {

  @Override
  public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {
    List<T> sortedList = copyToList(list);
    int size = sortedList.size();

    for (int i = 0; i < size - 1; i++) {
      int selectedIndex = i;
      for (int j = i + 1; j < size; j++) {
        if (comparator.compare(sortedList.get(j), sortedList.get(selectedIndex)) < 0) {
          selectedIndex = j;
        }
      }
      swap(sortedList, selectedIndex, i);
    }
    return sortedList;
  }
}
