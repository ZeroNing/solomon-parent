package com.steven.solomon.sort;

import com.steven.solomon.verification.ValidateUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 选择排序
 */
public class SelectionSortService implements SortService{

  @Override
  public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator, boolean ascending) {
    if(ValidateUtils.isEmpty(list)){
      return list;
    }
    List<T> sortedList = new ArrayList<>(list); // 创建一个副本以避免修改原始列表
    int n = sortedList.size();

    // 根据 ascending 参数决定是否反转比较器
    if (!ascending) {
      comparator = comparator.reversed(); // 如果是降序排序，反转比较器
    }

    for (int i = 0; i < n - 1; i++) {
      int selectedIndex = i;
      for (int j = i + 1; j < n; j++) {
        // 使用比较器比较元素
        if (comparator.compare(sortedList.get(j), sortedList.get(selectedIndex)) < 0) {
          selectedIndex = j; // 找到更小（或更大）的元素索引
        }
      }
      // 交换 selectedIndex 和 i 位置上的元素
      if (selectedIndex != i) {
        T temp = sortedList.get(selectedIndex);
        sortedList.set(selectedIndex, sortedList.get(i));
        sortedList.set(i, temp);
      }
    }

    return sortedList; // 返回排序后的列表
  }

  @Override
  public <T> Collection<T> sort(Collection<T> list,  List<Comparator<? super T>> comparators) {
    if(ValidateUtils.isEmpty(list)){
      return list;
    }
    List<T> sortedList = new ArrayList<>(list); // 创建一个副本以避免修改原始列表
    int n = sortedList.size();

    // 创建一个复合的Comparator
    Comparator<? super T> compositeComparator = null;
    for (Comparator comparator : comparators) {
      if (compositeComparator == null) {
        compositeComparator = comparator;
      } else {
        compositeComparator = compositeComparator.thenComparing(comparator);
      }
    }

    for (int i = 0; i < n - 1; i++) {
      int selectedIndex = i;
      for (int j = i + 1; j < n; j++) {
        // 使用复合比较器比较元素
        if (compositeComparator.compare(sortedList.get(j), sortedList.get(selectedIndex)) < 0) {
          selectedIndex = j; // 找到更小（或更大）的元素索引
        }
      }
      // 交换 selectedIndex 和 i 位置上的元素
      if (selectedIndex != i) {
        T temp = sortedList.get(selectedIndex);
        sortedList.set(selectedIndex, sortedList.get(i));
        sortedList.set(i, temp);
      }
    }

    return sortedList; // 返回排序后的列表
  }
}
