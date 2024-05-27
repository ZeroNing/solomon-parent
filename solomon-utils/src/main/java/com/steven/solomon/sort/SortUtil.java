package com.steven.solomon.sort;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class SortUtil {

  private static BubbleSortService bubbleSortService = new BubbleSortService();

  private static BucketSortService bucketSortService = new BucketSortService();

  private static HeapSortService heapSortService = new HeapSortService();

  private static InsertionSortService insertionSortService = new InsertionSortService();

  private static MergeSortService mergeSortService = new MergeSortService();

  private static QuickSortService quickSortService = new QuickSortService();

  private static SelectionSortService selectionSortService = new SelectionSortService();

  private static ShellSortService shellSortService = new ShellSortService();

  /**
   * 对给定的列表进行冒泡排序，支持升序和降序排序。
   *
   * @param <T>          列表元素的类型
   * @param list         要排序的列表
   * @param comparatorList   用于比较列表元素的比较器
   * @return 排序后的列表
   */
  public static <T> Collection<T> bubbleSort(Collection<T> list, Comparator<? super T>... comparatorList){
    return bubbleSortService.sort(list, Arrays.asList(comparatorList));
  }

  /**
   * 对给定的列表进行桶排序，支持升序和降序排序。
   *
   * @param <T>          列表元素的类型
   * @param list         要排序的列表
   * @param comparatorList   用于比较列表元素的比较器
   * @return 排序后的列表
   */
  public static <T> Collection<T> bucketSort(Collection<T> list, Comparator<? super T>... comparatorList){
    return bucketSortService.sort(list,Arrays.asList(comparatorList));
  }

  /**
   * 对给定的列表进行堆排序，支持升序和降序排序。
   *
   * @param <T>          列表元素的类型
   * @param list         要排序的列表
   * @param comparatorList   用于比较列表元素的比较器
   * @return 排序后的列表
   */
  public static <T> Collection<T> heapSort(Collection<T> list, Comparator<? super T>... comparatorList){
    return heapSortService.sort(list,Arrays.asList(comparatorList));
  }

  /**
   * 对给定的列表进行插入排序，支持升序和降序排序。
   *
   * @param <T>          列表元素的类型
   * @param list         要排序的列表
   * @param comparatorList   用于比较列表元素的比较器
   * @return 排序后的列表
   */
  public static <T> Collection<T> insertionSort(Collection<T> list, Comparator<? super T>... comparatorList){
    return insertionSortService.sort(list,Arrays.asList(comparatorList));
  }

  /**
   * 对给定的列表进行归并排序，支持升序和降序排序。
   *
   * @param <T>          列表元素的类型
   * @param list         要排序的列表
   * @param comparatorList   用于比较列表元素的比较器
   * @return 排序后的列表
   */
  public static <T> Collection<T> mergeSort(Collection<T> list, Comparator<? super T>... comparatorList){
    return mergeSortService.sort(list,Arrays.asList(comparatorList));
  }

  /**
   * 对给定的列表进行快速排序，支持升序和降序排序。
   *
   * @param <T>          列表元素的类型
   * @param list         要排序的列表
   * @param comparatorList   用于比较列表元素的比较器
   * @return 排序后的列表
   */
  public static <T> Collection<T> quickSort(Collection<T> list, Comparator<? super T>... comparatorList){
    return quickSortService.sort(list,Arrays.asList(comparatorList));
  }

  /**
   * 对给定的列表进行选择排序，支持升序和降序排序。
   *
   * @param <T>          列表元素的类型
   * @param list         要排序的列表
   * @param comparatorList   用于比较列表元素的比较器
   * @return 排序后的列表
   */
  public static <T> Collection<T> selectionSort(Collection<T> list, Comparator<? super T>... comparatorList){
    return selectionSortService.sort(list,Arrays.asList(comparatorList));
  }

  /**
   * 对给定的列表进行希尔排序，支持升序和降序排序。
   *
   * @param <T>          列表元素的类型
   * @param list         要排序的列表
   * @param comparatorList   用于比较列表元素的比较器
   * @return 排序后的列表
   */
  public static <T> Collection<T> shellSort(Collection<T> list, Comparator<? super T>... comparatorList){
    return shellSortService.sort(list,Arrays.asList(comparatorList));
  }

  /**
   * 对给定的列表进行冒泡排序，支持升序和降序排序。
   *
   * @param <T>          列表元素的类型
   * @param list         要排序的列表
   * @param comparator   用于比较列表元素的比较器
   * @param ascending    如果为 true，则进行升序排序；如果为 false，则进行降序排序
   * @return 排序后的列表
   */
  public static <T> Collection<T> bubbleSort(Collection<T> list, Comparator<? super T> comparator, boolean ascending){
    return bubbleSortService.sort(list,comparator,ascending);
  }

  /**
   * 对给定的列表进行桶排序，支持升序和降序排序。
   *
   * @param <T>          列表元素的类型
   * @param list         要排序的列表
   * @param comparator   用于比较列表元素的比较器
   * @param ascending    如果为 true，则进行升序排序；如果为 false，则进行降序排序
   * @return 排序后的列表
   */
  public static <T> Collection<T> bucketSort(Collection<T> list, Comparator<? super T> comparator, boolean ascending){
    return bucketSortService.sort(list,comparator,ascending);
  }

  /**
   * 对给定的列表进行堆排序，支持升序和降序排序。
   *
   * @param <T>          列表元素的类型
   * @param list         要排序的列表
   * @param comparator   用于比较列表元素的比较器
   * @param ascending    如果为 true，则进行升序排序；如果为 false，则进行降序排序
   * @return 排序后的列表
   */
  public static <T> Collection<T> heapSort(Collection<T> list, Comparator<? super T> comparator, boolean ascending){
    return heapSortService.sort(list,comparator,ascending);
  }

  /**
   * 对给定的列表进行插入排序，支持升序和降序排序。
   *
   * @param <T>          列表元素的类型
   * @param list         要排序的列表
   * @param comparator   用于比较列表元素的比较器
   * @param ascending    如果为 true，则进行升序排序；如果为 false，则进行降序排序
   * @return 排序后的列表
   */
  public static <T> Collection<T> insertionSort(Collection<T> list, Comparator<? super T> comparator, boolean ascending){
    return insertionSortService.sort(list,comparator,ascending);
  }

  /**
   * 对给定的列表进行归并排序，支持升序和降序排序。
   *
   * @param <T>          列表元素的类型
   * @param list         要排序的列表
   * @param comparator   用于比较列表元素的比较器
   * @param ascending    如果为 true，则进行升序排序；如果为 false，则进行降序排序
   * @return 排序后的列表
   */
  public static <T> Collection<T> mergeSort(Collection<T> list, Comparator<? super T> comparator, boolean ascending){
    return mergeSortService.sort(list,comparator,ascending);
  }

  /**
   * 对给定的列表进行快速排序，支持升序和降序排序。
   *
   * @param <T>          列表元素的类型
   * @param list         要排序的列表
   * @param comparator   用于比较列表元素的比较器
   * @param ascending    如果为 true，则进行升序排序；如果为 false，则进行降序排序
   * @return 排序后的列表
   */
  public static <T> Collection<T> quickSort(Collection<T> list, Comparator<? super T> comparator, boolean ascending){
    return quickSortService.sort(list,comparator,ascending);
  }

  /**
   * 对给定的列表进行选择排序，支持升序和降序排序。
   *
   * @param <T>          列表元素的类型
   * @param list         要排序的列表
   * @param comparator   用于比较列表元素的比较器
   * @param ascending    如果为 true，则进行升序排序；如果为 false，则进行降序排序
   * @return 排序后的列表
   */
  public static <T> Collection<T> selectionSort(Collection<T> list, Comparator<? super T> comparator, boolean ascending){
    return selectionSortService.sort(list,comparator,ascending);
  }

  /**
   * 对给定的列表进行希尔排序，支持升序和降序排序。
   *
   * @param <T>          列表元素的类型
   * @param list         要排序的列表
   * @param comparator   用于比较列表元素的比较器
   * @param ascending    如果为 true，则进行升序排序；如果为 false，则进行降序排序
   * @return 排序后的列表
   */
  public static <T> Collection<T> shellSort(Collection<T> list, Comparator<? super T> comparator, boolean ascending){
    return shellSortService.sort(list,comparator,ascending);
  }
}
