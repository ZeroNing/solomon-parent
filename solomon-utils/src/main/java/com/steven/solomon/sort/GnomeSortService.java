package com.steven.solomon.sort;

import java.util.*;

/**
 * 地精排序
 */
public class GnomeSortService implements SortService {

    @Override
    public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {
        // 将输入集合转换为列表，以便进行索引操作
        List<T> sortedList = new ArrayList<>(list);

        int index = 0;
        int n = sortedList.size();

        while (index < n) {
            if (index == 0 || comparator.compare(sortedList.get(index), sortedList.get(index - 1)) >= 0) {
                index++;
            } else {
                // 交换当前元素和前一个元素
                T temp = sortedList.get(index);
                sortedList.set(index, sortedList.get(index - 1));
                sortedList.set(index - 1, temp);
                index--;
            }
        }
        return sortedList;
    }
}
