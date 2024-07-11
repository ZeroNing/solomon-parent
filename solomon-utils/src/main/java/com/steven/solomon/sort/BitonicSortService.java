package com.steven.solomon.sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 双调排序
 */
public class BitonicSortService implements SortService {

    @Override
    public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {
        // 将输入集合转换为列表，以便进行索引操作
        List<T> sortedList = new ArrayList<>(list);
        int n = sortedList.size();

        // 调整为2的幂，因为双调排序要求元素数量必须是2的幂
        // 如果不是，需要填充到2的幂，这里为了简单起见，我们假设n已经是2的幂
        assert (n & (n - 1)) == 0 : "The number of elements must be a power of 2";

        // 开始双调排序过程
        bitonicSort(sortedList, 0, n, true, comparator);

        return sortedList;
    }

    // 递归实现双调序列的创建和比较
    private <T> void bitonicSort(List<T> list, int low, int count, boolean ascending, Comparator<? super T> comparator) {
        if (count > 1) {
            int k = count / 2;
            // 创建升序双调序列
            bitonicSort(list, low, k, true, comparator);
            // 创建降序双调序列
            bitonicSort(list, low + k, k, false, comparator);
            // 合并双调序列
            bitonicMerge(list, low, count, ascending, comparator);
        }
    }

    // 合并双调序列
    private <T> void bitonicMerge(List<T> list, int low, int count, boolean ascending, Comparator<? super T> comparator) {
        if (count > 1) {
            int k = greatestPowerOfTwoLessThan(count);
            for (int i = low; i < low + count - k; i++) {
                if (ascending == (comparator.compare(list.get(i), list.get(i + k)) > 0)) {
                    // 交换元素
                    T temp = list.get(i);
                    list.set(i, list.get(i + k));
                    list.set(i + k, temp);
                }
            }
            bitonicMerge(list, low, k, ascending, comparator);
            bitonicMerge(list, low + k, count - k, ascending, comparator);
        }
    }

    // 找到小于当前count的最大2的幂
    private int greatestPowerOfTwoLessThan(int count) {
        int k = 1;
        while (k < count) {
            k = k << 1;
        }
        return k >> 1;
    }
}
