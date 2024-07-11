package com.steven.solomon.sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 圈排序
 */
public class CycleSortService implements SortService {

    @Override
    public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {
        // 将输入集合转换为列表，以便进行索引操作
        List<T> sortedList = new ArrayList<>(list);
        int n = sortedList.size();

        // 开始圈排序过程
        for (int cycleStart = 0; cycleStart < n - 1; cycleStart++) {
            T item = sortedList.get(cycleStart);

            // 找到item的正确位置
            int pos = cycleStart;
            for (int i = cycleStart + 1; i < n; i++) {
                if (comparator.compare(sortedList.get(i), item) < 0) {
                    pos++;
                }
            }

            // 如果item已经在正确位置，继续下一个循环
            if (pos == cycleStart) {
                continue;
            }

            // 跳过重复元素
            while (comparator.compare(item, sortedList.get(pos)) == 0) {
                pos++;
            }

            // 将item放到正确位置，并将正确位置的元素保存到item
            if (pos != cycleStart) {
                T temp = item;
                item = sortedList.get(pos);
                sortedList.set(pos, temp);
            }

            // 旋转剩余的圈
            while (pos != cycleStart) {
                pos = cycleStart;

                // 找到item的正确位置
                for (int i = cycleStart + 1; i < n; i++) {
                    if (comparator.compare(sortedList.get(i), item) < 0) {
                        pos++;
                    }
                }

                // 跳过重复元素
                while (comparator.compare(item, sortedList.get(pos)) == 0) {
                    pos++;
                }

                // 将item放到正确位置，并将正确位置的元素保存到item
                if (comparator.compare(item, sortedList.get(pos)) != 0) {
                    T temp = item;
                    item = sortedList.get(pos);
                    sortedList.set(pos, temp);
                }
            }
        }

        return sortedList;
    }
}
