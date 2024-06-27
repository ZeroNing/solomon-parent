package com.steven.solomon.sort.enums;

import com.steven.solomon.sort.*;

public enum SortTypeEnum {

    BUBBLE_SORT("冒泡排序",new BubbleSortService()),
    BUCKET_SORT("桶排序",new BucketSortService()),
    HEAP_SORT("堆排序",new HeapSortService()),
    INSERTION_SORT("插入排序",new InsertionSortService()),
    MERGE_SORT("归并排序",new MergeSortService()),
    QUICK_SORT("快速排序",new QuickSortService()),
    SELECTION_SORT("选择排序",new SelectionSortService()),
    SHELL_SORT("希尔排序",new ShellSortService()),;

    private String desc;

    private SortService service;

    SortTypeEnum(String desc, SortService service) {
        this.desc = desc;
        this.service = service;
    }

    public SortService getService() {
        return service;
    }
}
