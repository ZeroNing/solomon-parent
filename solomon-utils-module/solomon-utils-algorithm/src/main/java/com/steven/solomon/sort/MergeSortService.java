package com.steven.solomon.sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 归并排序实现。
 *
 * <p>递归拆分集合，再合并两个有序子序列。合并逻辑集中在一个方法里，
 * 避免维护两套几乎相同的 merge 实现。</p>
 */
public class MergeSortService implements SortService {

  @Override
  public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {
    List<T> data = copyToList(list);
    if (data.size() <= 1) {
      return data;
    }

    int middle = data.size() / 2;
    List<T> left = copyToList(data.subList(0, middle));
    List<T> right = copyToList(data.subList(middle, data.size()));

    return merge(
        copyToList(sort(left, comparator)),
        copyToList(sort(right, comparator)),
        comparator);
  }

  /**
   * 合并两个已排序列表。
   */
  private <T> List<T> merge(List<T> left, List<T> right, Comparator<? super T> comparator) {
    List<T> result = new ArrayList<>(left.size() + right.size());
    int leftIndex = 0;
    int rightIndex = 0;

    while (leftIndex < left.size() && rightIndex < right.size()) {
      if (comparator.compare(left.get(leftIndex), right.get(rightIndex)) <= 0) {
        result.add(left.get(leftIndex++));
      } else {
        result.add(right.get(rightIndex++));
      }
    }
    result.addAll(left.subList(leftIndex, left.size()));
    result.addAll(right.subList(rightIndex, right.size()));
    return result;
  }
}
