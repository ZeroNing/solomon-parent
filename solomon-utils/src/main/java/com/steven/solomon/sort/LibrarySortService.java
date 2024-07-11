package com.steven.solomon.sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 图书馆排序
 */
public class LibrarySortService implements SortService {

    @Override
    public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {
        // 将输入集合转换为列表，以便进行索引操作
        List<T> sortedList = new ArrayList<>(list);

        // 初始化间隔
        int n = sortedList.size();
        int gap = (int) Math.sqrt(n);

        // 使用间隔进行插入排序
        for (int i = gap; i < n; i++) {
            T key = sortedList.get(i);
            int j = i;

            // 将元素插入到已排序序列中的正确位置
            while (j >= gap && comparator.compare(sortedList.get(j - gap), key) > 0) {
                sortedList.set(j, sortedList.get(j - gap));
                j -= gap;
            }
            sortedList.set(j, key);
        }

        // 最后进行一次标准的插入排序，以确保所有元素都已正确排序
        for (int i = 1; i < n; i++) {
            T key = sortedList.get(i);
            int j = i - 1;

            while (j >= 0 && comparator.compare(sortedList.get(j), key) > 0) {
                sortedList.set(j + 1, sortedList.get(j));
                j--;
            }
            sortedList.set(j + 1, key);
        }

        return sortedList;
    }
}
