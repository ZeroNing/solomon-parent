package com.steven.solomon.sort;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 堆排序实现。
 *
 * <p>先构建最大堆，再不断把堆顶元素移动到尾部。该算法原地调整复制后的列表，
 * 不会修改调用方传入的原始集合。</p>
 */
public class HeapSortService implements SortService {

  @Override
  public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {
    List<T> data = copyToList(list);
    int size = data.size();

    for (int i = size / 2 - 1; i >= 0; i--) {
      heapify(data, i, size, comparator);
    }

    for (int i = size - 1; i >= 0; i--) {
      swap(data, 0, i);
      heapify(data, 0, i, comparator);
    }
    return data;
  }

  /**
   * 从指定根节点开始调整最大堆。
   */
  private <T> void heapify(List<T> list, int root, int heapSize,
      Comparator<? super T> comparator) {
    int largest = root;
    int left = 2 * root + 1;
    int right = 2 * root + 2;

    if (left < heapSize && comparator.compare(list.get(left), list.get(largest)) > 0) {
      largest = left;
    }
    if (right < heapSize && comparator.compare(list.get(right), list.get(largest)) > 0) {
      largest = right;
    }
    if (largest == root) {
      return;
    }
    swap(list, root, largest);
    heapify(list, largest, heapSize, comparator);
  }
}
