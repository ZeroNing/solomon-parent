package com.steven.solomon.sort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
/**
 * 计数排序
 */
public class CountingSortService implements SortService{

  @Override
  public <T> List<T> sort(List<T> list, Comparator<? super T> comparator, boolean ascending) {
      if (list == null || list.isEmpty()) {
        return Collections.emptyList(); // 如果列表为空，直接返回空列表
      }

      // 找到列表中的最大值和最小值
      T min = Collections.min(list, comparator);
      T max = Collections.max(list, comparator);

      // 计算范围
      int range = comparator.compare(max, min) + 1;
      int[] count = new int[range];

      // 初始化计数数组
      for (T element : list) {
        int index = comparator.compare(element, min);
        count[index]++;
      }

      // 修改计数数组以记录位置
      if (ascending) {
        for (int i = 1; i < count.length; i++) {
          count[i] += count[i - 1];
        }
      } else {
        for (int i = count.length - 2; i >= 0; i--) {
          count[i] += count[i + 1];
        }
      }

      // 创建排序后的列表
      List<T> sortedList = new ArrayList<>(Collections.nCopies(list.size(), null));
      for (int i = list.size() - 1; i >= 0; i--) {
        T element = list.get(i);
        int index = comparator.compare(element, min);
        sortedList.set(count[index] - 1, element);
        count[index]--;
      }

      return sortedList;
  }
}
