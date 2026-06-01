package com.steven.solomon.sort;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 鸡尾酒排序实现。
 *
 * <p>鸡尾酒排序是双向冒泡排序：一轮正向把最大元素推到右边，
 * 再一轮反向把最小元素推到左边。</p>
 */
public class CocktailSortService implements SortService {

  @Override
  public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {
    List<T> dataList = copyToList(list);
    int start = 0;
    int end = dataList.size() - 1;
    boolean swapped;

    do {
      swapped = forwardPass(dataList, start, end, comparator);
      if (!swapped) {
        break;
      }
      end--;
      swapped = backwardPass(dataList, start, end, comparator);
      start++;
    } while (swapped);

    return dataList;
  }

  private <T> boolean forwardPass(List<T> list, int start, int end,
      Comparator<? super T> comparator) {
    boolean swapped = false;
    for (int i = start; i < end; i++) {
      if (comparator.compare(list.get(i), list.get(i + 1)) > 0) {
        swap(list, i, i + 1);
        swapped = true;
      }
    }
    return swapped;
  }

  private <T> boolean backwardPass(List<T> list, int start, int end,
      Comparator<? super T> comparator) {
    boolean swapped = false;
    for (int i = end - 1; i >= start; i--) {
      if (comparator.compare(list.get(i), list.get(i + 1)) > 0) {
        swap(list, i, i + 1);
        swapped = true;
      }
    }
    return swapped;
  }
}
