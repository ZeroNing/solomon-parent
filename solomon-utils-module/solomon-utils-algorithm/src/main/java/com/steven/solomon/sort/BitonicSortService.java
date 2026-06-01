package com.steven.solomon.sort;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 双调排序实现。
 *
 * <p>双调排序适合元素数量为 2 的幂的集合。这里保留原有约束，用断言提醒调用方；
 * 同时复用 {@link SortService} 的复制和交换方法，避免重复代码。</p>
 */
public class BitonicSortService implements SortService {

  @Override
  public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {
    List<T> sortedList = copyToList(list);
    int size = sortedList.size();
    assert (size & (size - 1)) == 0 : "The number of elements must be a power of 2";
    bitonicSort(sortedList, 0, size, true, comparator);
    return sortedList;
  }

  /**
   * 构造双调序列并按目标方向合并。
   */
  private <T> void bitonicSort(List<T> list, int low, int count, boolean ascending,
      Comparator<? super T> comparator) {
    if (count <= 1) {
      return;
    }
    int half = count / 2;
    bitonicSort(list, low, half, true, comparator);
    bitonicSort(list, low + half, half, false, comparator);
    bitonicMerge(list, low, count, ascending, comparator);
  }

  /**
   * 合并双调序列。
   */
  private <T> void bitonicMerge(List<T> list, int low, int count, boolean ascending,
      Comparator<? super T> comparator) {
    if (count <= 1) {
      return;
    }
    int step = greatestPowerOfTwoLessThan(count);
    for (int i = low; i < low + count - step; i++) {
      if (ascending == (comparator.compare(list.get(i), list.get(i + step)) > 0)) {
        swap(list, i, i + step);
      }
    }
    bitonicMerge(list, low, step, ascending, comparator);
    bitonicMerge(list, low + step, count - step, ascending, comparator);
  }

  /**
   * 获取小于 count 的最大 2 的幂。
   */
  private int greatestPowerOfTwoLessThan(int count) {
    int value = 1;
    while (value < count) {
      value <<= 1;
    }
    return value >> 1;
  }
}
