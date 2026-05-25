package com.steven.solomon.sort;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 插入排序实现。
 *
 * <p>适合小集合或基本有序的集合；每轮把当前元素插入左侧有序区间。</p>
 */
public class InsertionSortService implements SortService {

  @Override
  public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {
    List<T> sortedList = copyToList(list);
    for (int i = 1; i < sortedList.size(); i++) {
      insert(sortedList, i, comparator);
    }
    return sortedList;
  }

  /**
   * 把 index 位置的元素插入到左侧有序区间。
   */
  private <T> void insert(List<T> list, int index, Comparator<? super T> comparator) {
    T key = list.get(index);
    int previous = index - 1;
    while (previous >= 0 && comparator.compare(list.get(previous), key) > 0) {
      list.set(previous + 1, list.get(previous));
      previous--;
    }
    list.set(previous + 1, key);
  }
}
