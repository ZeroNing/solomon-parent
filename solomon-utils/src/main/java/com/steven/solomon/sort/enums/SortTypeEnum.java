package com.steven.solomon.sort.enums;

import com.steven.solomon.sort.BitonicSortService;
import com.steven.solomon.sort.BubbleSortService;
import com.steven.solomon.sort.BucketSortService;
import com.steven.solomon.sort.CocktailSortService;
import com.steven.solomon.sort.CombSortService;
import com.steven.solomon.sort.CycleSortService;
import com.steven.solomon.sort.GnomeSortService;
import com.steven.solomon.sort.HeapSortService;
import com.steven.solomon.sort.InsertionSortService;
import com.steven.solomon.sort.LibrarySortService;
import com.steven.solomon.sort.MergeSortService;
import com.steven.solomon.sort.QuickSortService;
import com.steven.solomon.sort.SelectionSortService;
import com.steven.solomon.sort.ShellSortService;
import com.steven.solomon.sort.SortService;

/**
 * 排序算法类型。
 *
 * <p>每个枚举持有一个无状态排序服务实例，调用方通过 {@link #getService()} 取得具体实现。
 * 排序服务不保存调用过程状态，因此枚举级复用实例是安全且高效的。</p>
 */
public enum SortTypeEnum {

  BUCKET_SORT("桶排序", new BucketSortService()),
  MERGE_SORT("归并排序", new MergeSortService()),
  SHELL_SORT("希尔排序", new ShellSortService()),
  HEAP_SORT("堆排序", new HeapSortService()),
  QUICK_SORT("快速排序", new QuickSortService()),
  INSERTION_SORT("插入排序", new InsertionSortService()),
  SELECTION_SORT("选择排序", new SelectionSortService()),
  BUBBLE_SORT("冒泡排序", new BubbleSortService()),
  COMB_SORT("梳排序", new CombSortService()),
  LIBRARY("Library Sort", new LibrarySortService()),
  GNOME("侏儒排序", new GnomeSortService()),
  BITON("双调排序", new BitonicSortService()),
  CYCLE("循环排序", new CycleSortService()),
  COCKTAIL("鸡尾酒排序", new CocktailSortService());

  private final String desc;
  private final SortService service;

  SortTypeEnum(String desc, SortService service) {
    this.desc = desc;
    this.service = service;
  }

  public SortService getService() {
    return service;
  }

  public String getDesc() {
    return desc;
  }
}
