package com.steven.solomon.sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 桶排序的通用比较器版本。
 *
 * <p>由于泛型对象无法直接计算数值区间，这里用比较器相对最小值的比较结果估算桶位置。
 * 对复杂对象更推荐使用标准库排序；该实现主要保留历史能力。</p>
 */
public class BucketSortService implements SortService {

  @Override
  public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {
    T min = Collections.min(list, comparator);
    T max = Collections.max(list, comparator);
    if (comparator.compare(max, min) == 0) {
      return copyToList(list);
    }

    List<List<T>> buckets = createBuckets(list.size());
    distribute(list, buckets, min, max, comparator);
    return mergeBuckets(buckets, comparator, list.size());
  }

  private <T> List<List<T>> createBuckets(int size) {
    int bucketCount = Math.max(1, (int) Math.sqrt(size));
    List<List<T>> buckets = new ArrayList<>(bucketCount);
    for (int i = 0; i < bucketCount; i++) {
      buckets.add(new ArrayList<>());
    }
    return buckets;
  }

  /**
   * 按比较器的相对结果把元素分配到桶。
   */
  private <T> void distribute(Collection<T> source, List<List<T>> buckets, T min, T max,
      Comparator<? super T> comparator) {
    long divisor = comparator.compare(max, min);
    for (T element : source) {
      int bucketIndex =
          (int) ((long) comparator.compare(element, min) * (buckets.size() - 1) / divisor);
      buckets.get(bucketIndex).add(element);
    }
  }

  private <T> List<T> mergeBuckets(List<List<T>> buckets, Comparator<? super T> comparator,
      int expectedSize) {
    List<T> sortedList = new ArrayList<>(expectedSize);
    for (List<T> bucket : buckets) {
      bucket.sort(comparator);
      sortedList.addAll(bucket);
    }
    return sortedList;
  }
}
