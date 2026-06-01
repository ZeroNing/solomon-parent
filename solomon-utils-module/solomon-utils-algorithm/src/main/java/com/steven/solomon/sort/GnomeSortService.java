package com.steven.solomon.sort;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 侏儒排序实现。
 *
 * <p>类似插入排序，通过相邻交换把较小元素逐步移动到前面。</p>
 */
public class GnomeSortService implements SortService {

  @Override
  public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {
    List<T> sortedList = copyToList(list);
    int index = 0;
    while (index < sortedList.size()) {
      if (index == 0 || comparator.compare(sortedList.get(index), sortedList.get(index - 1)) >= 0) {
        index++;
      } else {
        swap(sortedList, index, index - 1);
        index--;
      }
    }
    return sortedList;
  }
}
