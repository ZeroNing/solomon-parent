package com.steven.solomon.sort;

import java.util.*;

/**
 * 桶排序
 */
public class BucketSortService implements SortService {

    @Override
    public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator, boolean ascending) {
        if(!ascending){
            comparator = comparator.reversed();
        }
        return sort(list, Arrays.asList(comparator)); // 返回排序后的列表
    }

    @Override
    public <T> Collection<T> sort(Collection<T> list, List<Comparator<? super T>> comparators) {
        // 创建一个复合的Comparator
        Comparator<? super T> compositeComparator = null;
        for (Comparator comparator : comparators) {
            if (compositeComparator == null) {
                compositeComparator = comparator;
            } else {
                compositeComparator = compositeComparator.thenComparing(comparator);
            }
        }

        // 找到列表中的最大值和最小值
        T min = Collections.min(list, compositeComparator);
        T max = Collections.max(list, compositeComparator);
        if (compositeComparator.compare(max, min) == 0) {
            return new ArrayList<>(list);  // 如果min和max相等，所有元素都相同，直接返回
        }

        // 计算桶的数量
        int bucketCount = Math.max(1, (int) Math.sqrt(list.size()));  // 使用列表大小的平方根作为桶的数量
        List<List<T>> buckets = new ArrayList<>(bucketCount);
        for (int i = 0; i < bucketCount; i++) {
            buckets.add(new ArrayList<>());
        }

        // 将元素分配到各个桶中
        long divisor = compositeComparator.compare(max, min);
        for (T element : list) {
            int bucketIndex = (int) ((long) compositeComparator.compare(element, min) * (bucketCount - 1) / divisor);
            buckets.get(bucketIndex).add(element);
        }

        // 对每个桶内的元素进行排序
        for (List<T> bucket : buckets) {
            bucket.sort(compositeComparator);
        }

        // 合并所有桶中的元素
        List<T> sortedList = new ArrayList<>(list.size());
        for (List<T> bucket : buckets) {
            sortedList.addAll(bucket);
        }

        return sortedList;
    }
}
