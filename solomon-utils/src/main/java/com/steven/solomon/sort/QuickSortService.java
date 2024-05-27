package com.steven.solomon.sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 快速排序
 */
public class QuickSortService implements SortService{

  @Override
  public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator, boolean ascending) {
    List<T> sortedList = new ArrayList<>(list); // 创建一个副本以避免修改原始列表

    // 根据 ascending 参数决定是否反转比较器
    if (!ascending) {
      comparator = comparator.reversed(); // 如果是降序排序，反转比较器
    }

    quickSortHelper(sortedList, 0, sortedList.size() - 1, comparator);
    return sortedList; // 返回排序后的列表
  }

  @Override
  public <T> Collection<T> sort(Collection<T> list,  List<Comparator<? super T>> comparators) {
    List<T> sortedList = new ArrayList<>(list); // 创建一个副本以避免修改原始列表

    // 创建一个复合的Comparator
    Comparator<? super T> compositeComparator = null;
    for (Comparator comparator : comparators) {
      if (compositeComparator == null) {
        compositeComparator = comparator;
      } else {
        compositeComparator = compositeComparator.thenComparing(comparator);
      }
    }

    quickSortHelper(sortedList, 0, sortedList.size() - 1, compositeComparator);
    return sortedList; // 返回排序后的列表
  }

  private static <T> void quickSortHelper(List<T> list, int low, int high, Comparator<? super T> comparator) {
    if (low < high) {
      int pivotIndex = partition(list, low, high, comparator);
      quickSortHelper(list, low, pivotIndex - 1, comparator);
      quickSortHelper(list, pivotIndex + 1, high, comparator);
    }
  }

  private static <T> int partition(List<T> list, int low, int high, Comparator<? super T> comparator) {
    T pivot = list.get(high); // 选择高位为基准
    int i = low - 1; // 指定小于基准的区域

    for (int j = low; j < high; j++) {
      if (comparator.compare(list.get(j), pivot) <= 0) {
        i++;
        swap(list, i, j);
      }
    }
    swap(list, i + 1, high); // 将基准元素放置在正确位置
    return i + 1;
  }

  private static <T> void swap(List<T> list, int i, int j) {
    T temp = list.get(i);
    list.set(i, list.get(j));
    list.set(j, temp);
  }
}
