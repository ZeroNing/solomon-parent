package com.steven.solomon.sort.enums;

import com.steven.solomon.sort.*;

public enum SortTypeEnum {

    BUCKET_SORT("桶排序",new BucketSortService()),
    MERGE_SORT("归并排序",new MergeSortService()),
    SHELL_SORT("希尔排序",new ShellSortService()),
    HEAP_SORT("堆排序",new HeapSortService()),
    QUICK_SORT("快速排序",new QuickSortService()),
    INSERTION_SORT("插入排序",new InsertionSortService()),
    SELECTION_SORT("选择排序",new SelectionSortService()),
    BUBBLE_SORT("冒泡排序",new BubbleSortService()),
    COMB_SORT("梳排序",new CombSortService()),
    LIBRARY("图书馆排序",new LibrarySortService()),
    GNOME("地精排序",new GnomeSortService()),
    BITON("双调排序",new BitonicSortService()),
    CYCLE("圈排序",new CycleSortService()),
    ;

    private String desc;

    private SortService service;

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
