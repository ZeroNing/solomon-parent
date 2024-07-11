package com.steven.solomon.sort;

import java.util.*;

/**
 * 梳排序
 */
public class CombSortService implements SortService {

    @Override
    public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {
        // 将提供的集合转换为ArrayList，以便进行更简单的计算
        ArrayList<T> arrayList = new ArrayList<>(list);

        // 初始化梳排序的收缩因子和间隔
        double shrinkFactor = 1.3;
        int gap = arrayList.size();

        // 初始化swapped为false。它用于检查我们是否需要继续排序过程
        boolean swapped = false;

        // 当间隔大于1或者最后一次排序已经发生过交换时，继续排序
        while (gap > 1 || swapped) {
            // 如果间隔大于1，就将其除以收缩因子
            if (gap > 1) {
                gap = (int) (gap / shrinkFactor);
            }

            // 将swapped设置为false
            swapped = false;

            // 遍历列表，并比较当前元素和间隔后的元素
            for (int i = 0; gap + i < arrayList.size(); i++) {
                // 如果当前元素大于间隔后的元素，就交换它们
                if (comparator.compare(arrayList.get(i), arrayList.get(i + gap)) > 0) {
                    Collections.swap(arrayList, i, i + gap);
                    // 如果发生了交换，就将swapped设置为true
                    swapped = true;
                }
            }
        }

        // 返回排序后的列表
        return arrayList;
    }
}
