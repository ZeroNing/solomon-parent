package com.steven.solomon.sort;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public interface SortService {

   /**
    * 对给定的列表进行排序
    *
    * @param <T>          列表元素的类型
    * @param list         要排序的列表
    * @param comparators   用于比较列表元素的比较器
    * @return 排序后的列表
    */
   <T> Collection<T> sort(Collection<T> list, List<Comparator<? super T>> comparators);

   /**
    * 对给定的列表进行排序，支持升序和降序排序。
    *
    * @param <T>          列表元素的类型
    * @param list         要排序的列表
    * @param comparator   用于比较列表元素的比较器
    * @param ascending    如果为 true，则进行升序排序；如果为 false，则进行降序排序
    * @return 排序后的列表
    */
   <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator, boolean ascending);
}
