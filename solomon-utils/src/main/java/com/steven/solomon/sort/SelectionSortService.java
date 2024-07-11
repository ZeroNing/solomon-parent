package com.steven.solomon.sort;


import java.util.*;

/**
 * 选择排序
 */
public class SelectionSortService implements SortService {

    @Override
    public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {

        List<T> sortedList = new ArrayList<>(list); // 创建一个副本以避免修改原始列表
        int n = sortedList.size();

        for (int i = 0; i < n - 1; i++) {
            int selectedIndex = i;
            for (int j = i + 1; j < n; j++) {
                // 使用复合比较器比较元素
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
}
