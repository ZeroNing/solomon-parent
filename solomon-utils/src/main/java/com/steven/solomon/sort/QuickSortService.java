package com.steven.solomon.sort;

import java.util.*;

/**
 * 快速排序
 */
public class QuickSortService implements SortService {

    @Override
    public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {
        List<T> sortedList = new ArrayList<>(list); // 创建一个副本以避免修改原始列表

        quickSort(sortedList, comparator);
        return sortedList; // 返回排序后的列表
    }

    private static <T> void quickSort(List<T> list, Comparator<? super T> comparator) {
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[]{0, list.size() - 1});

        while (!stack.isEmpty()) {
            int[] range = stack.pop();
            int low = range[0];
            int high = range[1];

            if (low < high) {
                int pivotIndex = partition(list, low, high, comparator);
                stack.push(new int[]{low, pivotIndex - 1});
                stack.push(new int[]{pivotIndex + 1, high});
            }
        }
    }

    private static <T> int partition(List<T> list, int low, int high, Comparator<? super T> comparator) {
        T pivot = list.get(high); // 选择高位为基准
        int i = low - 1; // 指定小于基准的区域

        for (int j = low; j < high; j++) {
            if (comparator.compare(list.get(j), pivot) <= 0) {
                i++;
                swap(list, i, j);
            }
        }
        swap(list, i + 1, high); // 将基准元素放置在正确位置
        return i + 1;
    }

    private static <T> void swap(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

    private static <T> void quickSortHelper(List<T> list, int low, int high, Comparator<? super T> comparator) {
        if (low < high) {
            int pivotIndex = partition(list, low, high, comparator);
            quickSortHelper(list, low, pivotIndex - 1, comparator);
            quickSortHelper(list, pivotIndex + 1, high, comparator);
        }
    }
}
