package com.steven.solomon.sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 归并排序
 */
public class MergeSortService implements SortService{

  @Override
  public <T> Collection<T> sort(Collection<T> list, List<Comparator<T>> comparators) {
    if (list.size() <= 1) {
      return list;
    }
    List<T> data = new ArrayList<>(list);
    // 分割列表
    int middle = list.size() / 2;
    Collection<T> left = new ArrayList<>(data.subList(0, middle));
    Collection<T> right = new ArrayList<>(data.subList(middle, list.size()));

    // 递归排序左右两部分
    left = sort(left, comparators);
    right = sort(right, comparators);

    // 合并排序后的左右两部分
    return merge((List<T>) left, (List<T>) right, comparators);
  }

  private static <T> Collection<T> merge(List<T> left, List<T> right, List<Comparator<T>> comparators) {
    List<T> result = new ArrayList<>();
    int leftIndex = 0;
    int rightIndex = 0;

    // 创建一个复合的Comparator
    Comparator<T> compositeComparator = null;
    for (Comparator<T> comparator : comparators) {
      if (compositeComparator == null) {
        compositeComparator = comparator;
      } else {
        compositeComparator = compositeComparator.thenComparing(comparator);
      }
    }

    // 合并两个有序列表
    while (leftIndex < left.size() && rightIndex < right.size()) {
      if (compositeComparator.compare(left.get(leftIndex), right.get(rightIndex)) <= 0) {
        result.add(left.get(leftIndex));
        leftIndex++;
      } else {
        result.add(right.get(rightIndex));
        rightIndex++;
      }
    }

    // 添加剩余的元素
    while (leftIndex < left.size()) {
      result.add(left.get(leftIndex));
      leftIndex++;
    }

    while (rightIndex < right.size()) {
      result.add(right.get(rightIndex));
      rightIndex++;
    }

    return result;
  }
}
