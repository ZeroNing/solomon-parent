package com.steven.solomon.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 归并排序
 */
public class MergeSortService implements SortService{

  @Override
  public <T> List<T> sort(List<T> list, Comparator<? super T> comparator, boolean ascending) {
    if (list.size() <= 1) {
      return list;
    }

    // 分割列表
    int middle = list.size() / 2;
    List<T> left = new ArrayList<>(list.subList(0, middle));
    List<T> right = new ArrayList<>(list.subList(middle, list.size()));

    // 递归排序左右两部分
    left = sort(left, comparator, ascending);
    right = sort(right, comparator, ascending);

    // 合并排序后的左右两部分
    return merge(left, right, comparator, ascending);
  }

  private static <T> List<T> merge(List<T> left, List<T> right, Comparator<? super T> comparator, boolean ascending) {
    List<T> result = new ArrayList<>();
    int leftIndex = 0;
    int rightIndex = 0;

    // 合并两个有序列表
    while (leftIndex < left.size() && rightIndex < right.size()) {
      int comparison = comparator.compare(left.get(leftIndex), right.get(rightIndex));
      if (ascending ? comparison <= 0 : comparison >= 0) {
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
