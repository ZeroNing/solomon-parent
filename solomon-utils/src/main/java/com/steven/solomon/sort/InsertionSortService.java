package com.steven.solomon.sort;

import java.util.*;

/**
 * 插入排序
 */
public class InsertionSortService implements SortService {

    @Override
    public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {

        List<T> sortedList = new ArrayList<>(list); // 创建一个副本以避免修改原始列表
        int n = sortedList.size();

        for (int i = 1; i < n; i++) {
            T key = sortedList.get(i);
            int j = i - 1;

            // 将比 key 大的元素都向右移动
            while (j >= 0 && comparator.compare(sortedList.get(j), key) > 0) {
                sortedList.set(j + 1, sortedList.get(j));
                j--;
            }

            // 插入 key 到正确的位置
            sortedList.set(j + 1, key);
        }

        return sortedList; // 返回排序后的列表
    }
}
