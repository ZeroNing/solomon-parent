package com.steven.solomon.sort;

import com.steven.solomon.verification.ValidateUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 堆排序
 */
public class HeapSortService implements SortService {

    @Override
    public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator, boolean ascending) {

        int n = list.size();
        List<T> data = new ArrayList<>(list);
        // 构建最大堆
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(data, i, n, comparator);
        }

        // 一个一个地从堆中取出元素
        for (int i = n - 1; i >= 0; i--) {
            swap(data, 0, i);

            // 重新对堆进行调整
            heapify(data, 0, i, comparator);
        }

        // 如果需要升序排序，我们需要反转列表
        if (!ascending) {
            List<T> reversed = new ArrayList<>(data);
            for (int i = 0; i < data.size(); i++) {
                data.set(i, reversed.get(data.size() - 1 - i));
            }
        }

        return data;
    }

    @Override
    public <T> Collection<T> sort(Collection<T> list, List<Comparator<? super T>> comparators) {

        int n = list.size();
        List<T> data = new ArrayList<>(list);

        // 创建一个复合的Comparator
        Comparator<? super T> compositeComparator = null;
        for (Comparator comparator : comparators) {
            if (compositeComparator == null) {
                compositeComparator = comparator;
            } else {
                compositeComparator = compositeComparator.thenComparing(comparator);
            }
        }

        // 构建最大堆
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(data, i, n, compositeComparator);
        }

        // 一个一个地从堆中取出元素
        for (int i = n - 1; i >= 0; i--) {
            swap(data, 0, i);

            // 重新对堆进行调整
            heapify(data, 0, i, compositeComparator);
        }

        return data;
    }

    private static <T> void heapify(List<T> list, int i, int n, Comparator<? super T> comparator) {
        int largest = i; // 初始化最大值为根节点
        int left = 2 * i + 1; // 左子节点
        int right = 2 * i + 2; // 右子节点

        // 如果左子节点在范围内且大于根节点
        if (left < n && comparator.compare(list.get(left), list.get(largest)) > 0) {
            largest = left;
        }

        // 如果右子节点在范围内且大于当前最大值
        if (right < n && comparator.compare(list.get(right), list.get(largest)) > 0) {
            largest = right;
        }

        // 如果最大值不是根节点
        if (largest != i) {
            swap(list, i, largest);
            // 递归地调整受影响的子树
            heapify(list, largest, n, comparator);
        }
    }

    private static <T> void swap(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }

}
