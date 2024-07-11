package com.steven.solomon.sort;


import java.util.*;

/**
 * 希尔排序
 */
public class ShellSortService implements SortService {

    @Override
    public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {

        // 创建列表的副本，以避免修改原始列表
        List<T> sortedList = new ArrayList<>(list);

        // 初始化间隔gap为列表大小的一半
        int gap = sortedList.size() / 2;

        // 逐步减小间隔，直到gap为0
        while (gap > 0) {
            // 从gap位置开始遍历列表
            for (int i = gap; i < sortedList.size(); i++) {
                T temp = sortedList.get(i);
                int j = i;

                // 使用复合比较器进行比较
                while (j >= gap && comparator.compare(sortedList.get(j - gap), temp) > 0) {
                    sortedList.set(j, sortedList.get(j - gap));
                    j -= gap;
                }
                sortedList.set(j, temp);
            }
            // 缩小间隔
            gap /= 2;
        }

        // 返回排序后的列表
        return sortedList;
    }
}
