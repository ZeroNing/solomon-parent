package com.steven.solomon.sort;

import com.steven.solomon.sort.enums.SortTypeEnum;
import com.steven.solomon.verification.ValidateUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

public class SortUtil {

    /**
     * 对给定的列表进行排序，支持升序和降序排序。
     *
     * @param <T>            列表元素的类型
     * @param sortTypeEnum   排序类型
     * @param list           要排序的列表
     * @param comparator 用于比较列表元素的比较器
     * @return 排序后的列表
     */
    public static <T> Collection<T> sort(SortTypeEnum sortTypeEnum, Collection<T> list, Comparator<? super T> comparator) {
        if (ValidateUtils.isEmpty(list)) {
            return list;
        }
        return sortTypeEnum.getService().sort(list, comparator);
    }
}