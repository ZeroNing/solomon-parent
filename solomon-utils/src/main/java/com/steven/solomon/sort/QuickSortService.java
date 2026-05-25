package com.steven.solomon.sort;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

/**
 * 快速排序实现。
 *
 * <p>使用显式栈替代递归，避免大集合或极端数据下递归过深导致栈溢出。</p>
 */
public class QuickSortService implements SortService {

  @Override
  public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {
    List<T> sortedList = copyToList(list);
    quickSort(sortedList, comparator);
    return sortedList;
  }

  private <T> void quickSort(List<T> list, Comparator<? super T> comparator) {
    Stack<int[]> stack = new Stack<>();
    stack.push(new int[]{0, list.size() - 1});

    while (!stack.isEmpty()) {
      int[] range = stack.pop();
      int low = range[0];
      int high = range[1];
      if (low >= high) {
        continue;
      }
      int pivotIndex = partition(list, low, high, comparator);
      stack.push(new int[]{low, pivotIndex - 1});
      stack.push(new int[]{pivotIndex + 1, high});
    }
  }

  /**
   * 分区并返回基准元素最终位置。
   */
  private <T> int partition(List<T> list, int low, int high, Comparator<? super T> comparator) {
    T pivot = list.get(high);
    int smallerIndex = low - 1;
    for (int current = low; current < high; current++) {
      if (comparator.compare(list.get(current), pivot) <= 0) {
        smallerIndex++;
        swap(list, smallerIndex, current);
      }
    }
    swap(list, smallerIndex + 1, high);
    return smallerIndex + 1;
  }
}
