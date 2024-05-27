package com.steven.solomon.lambda;

import cn.hutool.core.math.MathUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.steven.solomon.sort.SortUtil;
import com.steven.solomon.verification.ValidateUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Lambda {

  /**
   * 转list
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param func      需要的字段
   */
  public static <T, S> List<T> toList(Collection<S> list, Predicate<S> predicate, Function<S, T> func,boolean isParallelStream) {
    if (ValidateUtils.isEmpty(list)) {
      return new ArrayList<>();
    }
    
    Stream<S> stream = isParallelStream ? list.parallelStream() : list.stream();
    if(ValidateUtils.isNotEmpty(predicate)){
      stream = stream.filter(predicate);
    }
    return stream.map(func).collect(Collectors.toList());
  }

  /**
   * 转list
   *
   * @param list 数据集合
   * @param func 需要的字段
   */
  public static <T, S> List<T> toList(Collection<S> list, Function<S, T> func,boolean isParallelStream) {
    return toList(list,null,func,isParallelStream);
  }

  /**
   * 转list
   *
   * @param list 数据集合
   * @param func 需要的字段
   */
  public static <T, S> List<T> toList(Collection<S> list, Function<S, T> func) {
    return toList(list,null,func,false);
  }

  /**
   * 转set
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param func      需要的字段
   */
  public static <T, S> Set<T> toSet(Collection<S> list, Predicate<S> predicate, Function<S, T> func,boolean isParallelStream) {
    if (ValidateUtils.isEmpty(list)) {
      return new HashSet<>();
    }
    
    Stream<S> stream = isParallelStream ? list.parallelStream() : list.stream();
    if(ValidateUtils.isNotEmpty(predicate)){
      stream = stream.filter(predicate);
    }
    return stream.map(func).collect(Collectors.toSet());
  }

  /**
   * 转set
   *
   * @param list 数据集合
   * @param func 需要的字段
   */
  public static <T, S> Set<T> toSet(Collection<S> list, Function<S, T> func,boolean isParallelStream) {
    return toSet(list,null,func,isParallelStream);
  }

  /**
   * 转set
   *
   * @param list 数据集合
   * @param func 需要的字段
   */
  public static <T, S> Set<T> toSet(Collection<S> list, Function<S, T> func) {
    return toSet(list,null,func,false);
  }

  /**
   * 转map
   *
   * @param list    数据集合
   * @param keyFunc 需要的字段
   */
  public static <K, T> Map<K, T> toMap(Collection<T> list, Function<T, K> keyFunc,boolean isParallelStream) {
    if (ValidateUtils.isEmpty(list)) {
      return new HashMap<>();
    }
    
    Stream<T> stream = isParallelStream ? list.parallelStream() : list.stream();
    return stream.collect(Collectors.toMap(keyFunc, Function.identity(), (key1, key2) -> key2));
  }

  /**
   * 转map
   *
   * @param list    数据集合
   * @param keyFunc 需要的字段
   */
  public static <K, T> Map<K, T> toMap(Collection<T> list, Function<T, K> keyFunc) {
    return toMap(list,keyFunc,false);
  }

  /**
   * 转map
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param keyFunc   需要的字段
   */
  public static <K, T> Map<K, T> toMapPredicate(Collection<T> list, Predicate<T> predicate, Function<T, K> keyFunc) {
    return toMapPredicate(list,predicate,keyFunc,false);
  }

  /**
   * 转map
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param keyFunc   需要的字段
   */
  public static <K, T> Map<K, T> toMapPredicate(Collection<T> list, Predicate<T> predicate, Function<T, K> keyFunc,boolean isParallelStream) {
    if (ValidateUtils.isEmpty(list)) {
      return new HashMap<>();
    }
    
    Stream<T> stream = isParallelStream ? list.parallelStream() : list.stream();
    if(ValidateUtils.isNotEmpty(predicate)){
      stream = stream.filter(predicate);
    }
    return stream.collect(Collectors.toMap(keyFunc, Function.identity(), (key1, key2) -> key2));
  }

  /**
   * 转map
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param keyFunc   需要的字段
   * @param valFunc   需要的字段
   */
  public static <K, V, T> Map<K, V> toMapPredicate(Collection<T> list, Predicate<T> predicate, Function<T, K> keyFunc,
      Function<T, V> valFunc,boolean isParallelStream) {
    if (ValidateUtils.isEmpty(list)) {
      return new HashMap<>();
    }
    
    Stream<T> stream = isParallelStream ? list.parallelStream() : list.stream();
    if(ValidateUtils.isNotEmpty(predicate)){
      stream = stream.filter(predicate);
    }
    return stream.collect(Collectors.toMap(keyFunc, valFunc, (key1, key2) -> key2));
  }

  /**
   * 转map
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param keyFunc   需要的字段
   * @param valFunc   需要的字段
   */
  public static <K, V, T> Map<K, V> toMapPredicate(Collection<T> list, Predicate<T> predicate, Function<T, K> keyFunc,
      Function<T, V> valFunc) {
    return toMapPredicate(list,predicate,keyFunc,valFunc,false);
  }

  /**
   * 转map
   *
   * @param list    数据集合
   * @param keyFunc 需要的字段
   * @param valFunc 需要的字段
   */
  public static <K, V, T> Map<K, V> toMap(Collection<T> list, Function<T, K> keyFunc, Function<T, V> valFunc,boolean isParallelStream) {
    if (ValidateUtils.isEmpty(list)) {
      return new HashMap<>();
    }
    
    Stream<T> stream = isParallelStream ? list.parallelStream() : list.stream();
    return stream.collect(Collectors.toMap(keyFunc, valFunc, (key1, key2) -> key2));
  }

  /**
   * 转map
   *
   * @param list    数据集合
   * @param keyFunc 需要的字段
   * @param valFunc 需要的字段
   */
  public static <K, V, T> Map<K, V> toMap(Collection<T> list, Function<T, K> keyFunc, Function<T, V> valFunc) {
    return toMap(list,keyFunc,valFunc,false);
  }

  /**
   * 分组
   *
   * @param list        数据集合
   * @param predicate   条件筛选数据
   * @param groupByFunc 分组需要字段
   */
  public static <K, T> Map<K, List<T>> groupBy(Collection<T> list, Predicate<T> predicate, Function<T, K> groupByFunc,boolean isParallelStream) {
    if (ValidateUtils.isEmpty(list)) {
      return new HashMap<>();
    }
    
    Stream<T> stream = isParallelStream ? list.parallelStream() : list.stream();
    if(ValidateUtils.isNotEmpty(predicate)){
      stream = stream.filter(predicate);
    }
    return stream.collect(Collectors.groupingBy(groupByFunc));
  }

  /**
   * 分组
   *
   * @param list        数据集合
   * @param predicate   条件筛选数据
   * @param groupByFunc 分组需要字段
   */
  public static <K, T> Map<K, List<T>> groupBy(Collection<T> list, Predicate<T> predicate, Function<T, K> groupByFunc) {
    return groupBy(list,predicate,groupByFunc,false);
  }

  /**
   * 分组
   *
   * @param list        数据集合
   * @param groupByFunc 分组需要字段
   */
  public static <K, T> Map<K, List<T>> groupBy(Collection<T> list, Function<T, K> groupByFunc) {
    if (ValidateUtils.isEmpty(list)) {
      return new HashMap<>();
    }
    return groupBy(list,null,groupByFunc,false);
  }

  /**
   * 汇总
   *
   * @param list 数据集合
   *             ram func 分组汇总相加的字段
   */
  public static <T> Integer sum(Collection<T> list, ToIntFunction<T> func) {
    return sum(list,null,func,false);
  }

  /**
   * 汇总
   *
   * @param list 数据集合
   *             ram func 分组汇总相加的字段
   */
  public static <T> Integer sum(Collection<T> list, ToIntFunction<T> func,boolean isParallelStream) {
    return sum(list,null,func,isParallelStream);
  }

  /**
   * 汇总
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param func      分组汇总相加的字段
   */
  public static <T> Integer sum(Collection<T> list, Predicate<T> predicate, ToIntFunction<T> func,boolean isParallelStream) {
    if(ValidateUtils.isEmpty(list)){
      return 0;
    }
    
    Stream<T> stream = isParallelStream ? list.parallelStream() : list.stream();
    if(ValidateUtils.isNotEmpty(predicate)){
      stream = stream.filter(predicate);
    }
    return stream.mapToInt(func).sum();
  }

  /**
   * 汇总
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param func      分组汇总相加的字段
   */
  public static <T> Integer sum(Collection<T> list, Predicate<T> predicate, ToIntFunction<T> func) {
    return sum(list,predicate,func,false);
  }

  /**
   * 汇总
   *
   * @param list 数据集合
   * @param func 分组汇总相加的字段
   */
  public static <T> Long sum(Collection<T> list, ToLongFunction<T> func) {
    return sum(list,null,func,false);
  }

  /**
   * 汇总
   *
   * @param list 数据集合
   * @param func 分组汇总相加的字段
   */
  public static <T> Long sum(Collection<T> list, ToLongFunction<T> func,boolean isParallelStream) {
    return sum(list,null,func,isParallelStream);
  }

  /**
   * 汇总
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param func      分组汇总相加的字段
   */
  public static <T> Long sum(Collection<T> list, Predicate<T> predicate, ToLongFunction<T> func,boolean isParallelStream) {
    if(ValidateUtils.isEmpty(list)){
      return 0L;
    }
    
    Stream<T> stream = isParallelStream ? list.parallelStream() : list.stream();
    if(ValidateUtils.isNotEmpty(predicate)){
      stream = stream.filter(predicate);
    }
    return stream.mapToLong(func).sum();
  }

  /**
   * 汇总
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param func      分组汇总相加的字段
   */
  public static <T> Long sum(Collection<T> list, Predicate<T> predicate, ToLongFunction<T> func) {
    return sum(list,predicate,func,false);
  }

  /**
   * 汇总
   *
   * @param list 数据集合
   * @param func 分组汇总相加的字段
   */
  public static <T, K> Double sum(Collection<T> list, ToDoubleFunction<T> func) {
    return sum(list,func,false);
  }

  /**
   * 汇总
   *
   * @param list 数据集合
   * @param func 分组汇总相加的字段
   */
  public static <T, K> Double sum(Collection<T> list, ToDoubleFunction<T> func,boolean isParallelStream) {
    return sum(list,func,isParallelStream);
  }

  /**
   * 汇总
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param func      分组汇总相加的字段
   */
  public static <T> Double sum(Collection<T> list, Predicate<T> predicate, ToDoubleFunction<T> func,boolean isParallelStream) {
    if(ValidateUtils.isEmpty(list)){
      return 0d;
    }
    
    Stream<T> stream = isParallelStream ? list.parallelStream() : list.stream();
    if(ValidateUtils.isNotEmpty(predicate)){
      stream = stream.filter(predicate);
    }
    return stream.mapToDouble(func).sum();
  }

  /**
   * 汇总
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param func      分组汇总相加的字段
   */
  public static <T> Double sum(Collection<T> list, Predicate<T> predicate, ToDoubleFunction<T> func) {
    return sum(list,predicate,func,false);
  }

  /**
   * 分组统计
   *
   * @param list        数据集合
   * @param groupByFunc 分组统计的字段
   */
  public static <K, T> Map<K, Long> groupByCount(Collection<T> list, Function<T, K> groupByFunc) {
    if (ValidateUtils.isEmpty(list)) {
      return new HashMap<>();
    }
    
    return list.stream().collect(Collectors.groupingBy(groupByFunc, Collectors.counting()));
  }

  /**
   * 分组统计
   *
   * @param list        数据集合
   * @param predicate   条件筛选数据
   * @param groupByFunc 分组统计的字段
   */
  public static <K, T> Map<K, Long> groupByCount(Collection<T> list, Function<T, K> groupByFunc,
      Predicate<T> predicate) {
    if (ValidateUtils.isEmpty(list)) {
      return new HashMap<>();
    }
    
    return list.stream().filter(predicate).collect(Collectors.groupingBy(groupByFunc, Collectors.counting()));
  }

  /**
   * 分组汇总
   *
   * @param list        数据集合
   * @param groupByFunc 分组字段
   * @param sumFunc     分组汇总相加的字段
   */
  public static <K, T> Map<K, Integer> groupBySum(Collection<T> list, Function<T, K> groupByFunc,
      ToIntFunction<T> sumFunc) {
    if (ValidateUtils.isEmpty(list)) {
      return new HashMap<>();
    }
    
    return list.stream().collect(Collectors.groupingBy(groupByFunc, Collectors.summingInt(sumFunc)));
  }

  /**
   * 分组汇总
   *
   * @param list        数据集合
   * @param groupByFunc 分组字段
   * @param predicate   条件筛选数据
   * @param sumFunc     分组汇总相加的字段
   */
  public static <K, T> Map<K, Integer> groupBySum(Collection<T> list, Function<T, K> groupByFunc,
      Predicate<T> predicate, ToIntFunction<T> sumFunc) {
    if (ValidateUtils.isEmpty(list)) {
      return new HashMap<>();
    }
    
    return list.stream().filter(predicate).collect(Collectors.groupingBy(groupByFunc, Collectors.summingInt(sumFunc)));
  }

  /**
   * 分组汇总
   *
   * @param list        数据集合
   * @param groupByFunc 分组字段
   * @param sumFunc     分组汇总相加的字段
   */
  public static <K, T> Map<K, Long> groupBySum(Collection<T> list, Function<T, K> groupByFunc,
      ToLongFunction<T> sumFunc) {
    if (ValidateUtils.isEmpty(list)) {
      return new HashMap<>();
    }
    
    return list.stream().collect(Collectors.groupingBy(groupByFunc, Collectors.summingLong(sumFunc)));
  }

  /**
   * 分组汇总
   *
   * @param list        数据集合
   * @param groupByFunc 分组字段
   * @param predicate   条件筛选数据
   * @param sumFunc     分组汇总相加的字段
   */
  public static <K, T> Map<K, Long> groupBySum(Collection<T> list, Function<T, K> groupByFunc, Predicate<T> predicate,
      ToLongFunction<T> sumFunc) {
    if (ValidateUtils.isEmpty(list)) {
      return new HashMap<>();
    }
    
    return list.stream().filter(predicate).collect(Collectors.groupingBy(groupByFunc, Collectors.summingLong(sumFunc)));
  }

  /**
   * 分组汇总
   *
   * @param list        数据集合
   * @param groupByFunc 分组字段
   * @param sumFunc     分组汇总相加的字段
   */
  public static <K, T> Map<K, Double> groupBySum(Collection<T> list, Function<T, K> groupByFunc,
      ToDoubleFunction<T> sumFunc) {
    if (ValidateUtils.isEmpty(list)) {
      return new HashMap<>();
    }
    
    return list.stream().collect(Collectors.groupingBy(groupByFunc, Collectors.summingDouble(sumFunc)));
  }

  /**
   * 分组汇总
   *
   * @param list        数据集合
   * @param groupByFunc 分组字段
   * @param predicate   条件筛选数据
   * @param sumFunc     分组汇总相加的字段
   */
  public static <K, T> Map<K, Double> groupBySum(Collection<T> list, Function<T, K> groupByFunc, Predicate<T> predicate,
      ToDoubleFunction<T> sumFunc) {
    if (ValidateUtils.isEmpty(list)) {
      return new HashMap<>();
    }
    
    return list.stream().filter(predicate)
        .collect(Collectors.groupingBy(groupByFunc, Collectors.summingDouble(sumFunc)));
  }

  /**
   * 交叉集
   *
   * @param sourceList 数据集合
   * @param predicate  条件筛选数据
   */
  public static <T> List<T> cross(Collection<T> sourceList, Predicate<T> predicate) {
    if (ValidateUtils.isEmpty(sourceList)) {
      return new ArrayList<>();
    }
    return sourceList.stream().filter(predicate).collect(Collectors.toList());
  }

  /**
   * 找出最大值
   *
   * @param list 数据集合
   *             ram func 分组汇总相加的字段
   */
  public static <S> S max(Collection<S> list, Comparator<? super S> comparator) {
    if(ValidateUtils.isEmpty(list)){
      return null;
    }
    
    return list.stream().max(comparator).get();
  }

  /**
   * 找出最大值
   *
   * @param list 数据集合
   *             ram func 分组汇总相加的字段
   */
  public static <T> Integer max(Collection<T> list, ToIntFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return null;
    }
    
    return list.stream().mapToInt(func).max().getAsInt();
  }

  /**
   * 找出最大值
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param func      分组汇总相加的字段
   */
  public static <T> Integer max(Collection<T> list, Predicate<T> predicate, ToIntFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return null;
    }
    
    return list.stream().filter(predicate).mapToInt(func).max().getAsInt();
  }

  /**
   * 找出最大值
   *
   * @param list 数据集合
   * @param func 分组汇总相加的字段
   */
  public static <T> Long max(Collection<T> list, ToLongFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return null;
    }
    
    return list.stream().mapToLong(func).max().getAsLong();
  }

  /**
   * 找出最大值
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param func      分组汇总相加的字段
   */
  public static <T> Long max(Collection<T> list, Predicate<T> predicate, ToLongFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return null;
    }
    
    return list.stream().filter(predicate).mapToLong(func).max().getAsLong();
  }

  /**
   * 找出最大值
   *
   * @param list 数据集合
   * @param func 分组汇总相加的字段
   */
  public static <T, K> Double max(Collection<T> list, ToDoubleFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return null;
    }
    
    return list.stream().mapToDouble(func).max().getAsDouble();
  }

  /**
   * 找出最大值
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param func      分组汇总相加的字段
   */
  public static <T> Double max(Collection<T> list, Predicate<T> predicate, ToDoubleFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return null;
    }
    
    return list.stream().filter(predicate).mapToDouble(func).max().getAsDouble();
  }

  /**
   * 找出最大值
   *
   * @param list 数据集合
   *             ram func 分组汇总相加的字段
   */
  public static <S> S min(Collection<S> list, Comparator<? super S> comparator) {
    if(ValidateUtils.isEmpty(list)){
      return null;
    }
    
    return list.stream().min(comparator).get();
  }

  /**
   * 找出最小值
   *
   * @param list 数据集合
   *             ram func 分组汇总相加的字段
   */
  public static <T> Integer min(Collection<T> list, ToIntFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return null;
    }
    
    return list.stream().mapToInt(func).min().getAsInt();
  }

  /**
   * 找出最小值
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param func      分组汇总相加的字段
   */
  public static <T> Integer min(Collection<T> list, Predicate<T> predicate, ToIntFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return null;
    }
    
    return list.stream().filter(predicate).mapToInt(func).min().getAsInt();
  }

  /**
   * 找出最小值
   *
   * @param list 数据集合
   * @param func 分组汇总相加的字段
   */
  public static <T> Long min(Collection<T> list, ToLongFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return null;
    }
    
    return list.stream().mapToLong(func).min().getAsLong();
  }

  /**
   * 找出最小值
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param func      分组汇总相加的字段
   */
  public static <T> Long min(Collection<T> list, Predicate<T> predicate, ToLongFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return null;
    }
    
    return list.stream().filter(predicate).mapToLong(func).min().getAsLong();
  }

  /**
   * 找出最小值
   *
   * @param list 数据集合
   * @param func 分组汇总相加的字段
   */
  public static <T, K> Double min(Collection<T> list, ToDoubleFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return null;
    }
    
    return list.stream().mapToDouble(func).min().getAsDouble();
  }

  /**
   * 找出最小值
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param func      分组汇总相加的字段
   */
  public static <T> Double min(Collection<T> list, Predicate<T> predicate, ToDoubleFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return null;
    }
    
    return list.stream().filter(predicate).mapToDouble(func).min().getAsDouble();
  }

  /**
   * 平均值
   *
   * @param list 数据集合
   *             ram func 分组汇总相加的字段
   */
  public static <T> Double average(Collection<T> list, ToIntFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return null;
    }
    
    return list.stream().mapToInt(func).average().getAsDouble();
  }

  /**
   * 平均值
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param func      分组汇总相加的字段
   */
  public static <T> Double average(Collection<T> list, Predicate<T> predicate, ToIntFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return null;
    }
    
    return list.stream().filter(predicate).mapToInt(func).average().getAsDouble();
  }

  /**
   * 平均值
   *
   * @param list 数据集合
   * @param func 分组汇总相加的字段
   */
  public static <T> Double average(Collection<T> list, ToLongFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return null;
    }
    
    return list.stream().mapToLong(func).average().getAsDouble();
  }

  /**
   * 平均值
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param func      分组汇总相加的字段
   */
  public static <T> Double average(Collection<T> list, Predicate<T> predicate, ToLongFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return null;
    }
    
    return list.stream().filter(predicate).mapToLong(func).average().getAsDouble();
  }

  /**
   * 平均值
   *
   * @param list 数据集合
   * @param func 分组汇总相加的字段
   */
  public static <T, K> Double average(Collection<T> list, ToDoubleFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return null;
    }
    
    return list.stream().mapToDouble(func).average().getAsDouble();
  }

  /**
   * 平均值
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param func      分组汇总相加的字段
   */
  public static <T> Double average(Collection<T> list, Predicate<T> predicate, ToDoubleFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return null;
    }
    
    return list.stream().filter(predicate).mapToDouble(func).average().getAsDouble();
  }

  /**
   * 排序
   *
   * @param list       数据集合
   * @param comparator 条件排序(Comparator.comparing)
   */
  public static <T> List<T> sort(Collection<T> list, Comparator<? super T> comparator) {
    if(ValidateUtils.isEmpty(list)){
      return new ArrayList<>();
    }
    
    return list.stream().sorted(comparator).collect(Collectors.toList());
  }

  /**
   * 获取各种汇总数据
   *
   * @param list 数据集合
   *             ram func 分组汇总相加的字段
   */
  public static <T> IntSummaryStatistics summaryStatistics(Collection<T> list, ToIntFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return new IntSummaryStatistics();
    }
    
    return list.stream().mapToInt(func).summaryStatistics();
  }

  /**
   * 获取各种汇总数据
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param func      分组汇总相加的字段
   */
  public static <T> IntSummaryStatistics summaryStatistics(Collection<T> list, Predicate<T> predicate,
      ToIntFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return new IntSummaryStatistics();
    }
    
    return list.stream().filter(predicate).mapToInt(func).summaryStatistics();
  }

  /**
   * 获取各种汇总数据
   *
   * @param list 数据集合
   * @param func 分组汇总相加的字段
   */
  public static <T> LongSummaryStatistics summaryStatistics(Collection<T> list, ToLongFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return new LongSummaryStatistics();
    }
    
    return list.stream().mapToLong(func).summaryStatistics();
  }

  /**
   * 获取各种汇总数据
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param func      分组汇总相加的字段
   */
  public static <T> LongSummaryStatistics summaryStatistics(Collection<T> list, Predicate<T> predicate,
      ToLongFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return new LongSummaryStatistics();
    }
    
    return list.stream().filter(predicate).mapToLong(func).summaryStatistics();
  }

  /**
   * 获取各种汇总数据
   *
   * @param list 数据集合
   * @param func 分组汇总相加的字段
   */
  public static <T> DoubleSummaryStatistics summaryStatistics(Collection<T> list, ToDoubleFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return new DoubleSummaryStatistics();
    }
    
    return list.stream().mapToDouble(func).summaryStatistics();
  }

  /**
   * 获取各种汇总数据
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   * @param func      分组汇总相加的字段
   */
  public static <T> DoubleSummaryStatistics summaryStatistics(Collection<T> list, Predicate<T> predicate,
      ToDoubleFunction<T> func) {
    if(ValidateUtils.isEmpty(list)){
      return new DoubleSummaryStatistics();
    }
    
    return list.stream().filter(predicate).mapToDouble(func).summaryStatistics();
  }

  /**
   * 去重
   * @param list 数据集合
   * @param func 需要的字段
   */
  public static <T,S> List<S> distinct(List<T> list, Function<T, S> func){
    if(ValidateUtils.isEmpty(list)){
      return new ArrayList<>();
    }
    
    return list.stream().map(func).distinct().collect(Collectors.toList());
  }

  /**
   * 匹配 判断的条件里，任意一个元素成功，返回true
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   */
  public static <T> boolean anyMatch(List<T> list,Predicate<T> predicate){
    if(ValidateUtils.isEmpty(list)){
      return false;
    }
    
    return list.stream().anyMatch(predicate);
  }

  /**
   * 匹配 判断条件里的元素，所有的都是，返回true
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   */
  public static <T> boolean allMatch(List<T> list,Predicate<T> predicate){
    if(ValidateUtils.isEmpty(list)){
      return false;
    }
    
    return list.stream().allMatch(predicate);
  }

  /**
   * 匹配 noneMatch跟allMatch相反，判断条件里的元素，所有的都不是，返回true
   *
   * @param list      数据集合
   * @param predicate 条件筛选数据
   */
  public static <T> boolean noneMatch(List<T> list,Predicate<T> predicate){
    if(ValidateUtils.isEmpty(list)){
      return false;
    }
    
    return list.stream().noneMatch(predicate);
  }

  /**
   * 找出重复记录
   * @param list 数据集合
   * @param groupByFunc 分组条件
   */
  public static <K, T> List<K> repeat(List<T> list, Function<T, K> groupByFunc){
    if(ValidateUtils.isEmpty(list)){
      return new ArrayList<>();
    }
    
    Map<K, List<T>> groupByMap = groupBy(list,groupByFunc);
    return groupByMap.entrySet().stream().filter(entry -> entry.getValue().size() > 1).map(entry-> entry.getKey()).collect(Collectors.toList());
  }

  public static <T> void foreach(List<T> list, Consumer<T> consumer){
    if(ValidateUtils.isEmpty(list)){
      return;
    }
    for(T t : list){
      consumer.accept(t);
    }
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
  public static <T> List<T> bubbleSort(List<T> list, Comparator<? super T> comparator, boolean ascending) {
    List<T> sortedList = new ArrayList<>(list); // 创建一个副本以避免修改原始列表
    boolean swapped;
    int n = sortedList.size();

    // 根据 ascending 参数决定是否反转比较器
    if (!ascending) {
      comparator = comparator; // 如果是降序排序，反转比较器
    }

    do {
      swapped = false;
      for (int i = 1; i < n; i++) {
        // 使用比较器比较相邻元素
        if (comparator.compare(sortedList.get(i - 1), sortedList.get(i)) > 0) {
          // 如果前一个元素大于后一个元素，则交换它们
          Collections.swap(sortedList, i - 1, i);
          swapped = true;
        }
      }
      // 减少下一次遍历的范围，因为每轮遍历都会将最大的元素移动到正确的位置
      n--;
    } while (swapped); // 如果某轮遍历没有发生交换，说明列表已经排序完成

    return sortedList; // 返回排序后的列表
  }


  public static void main(String[] args) {
    Set<Integer> a = new HashSet<>();
    for(Integer i = 0;i<=20;i++){
      a.add(Integer.valueOf((int) (Math.random()*100)));
    }

    List<Person> b = new ArrayList<>();
    b.add(new Person("1",1));
    b.add(new Person("2",2));

    System.out.println("冒泡排序算法：降序:"+ SortUtil.bubbleSort(b,Comparator.comparing(Person::getName).reversed(),Comparator.comparing(Person::getAge).reversed()));
    System.out.println("桶排序算法:  降序:"+ SortUtil.bucketSort(b,Comparator.comparing(Person::getName).reversed(),Comparator.comparing(Person::getAge).reversed()));
    System.out.println("堆排序算法:  降序:"+ SortUtil.heapSort(b,Comparator.comparing(Person::getName).reversed(),Comparator.comparing(Person::getAge).reversed()));
    System.out.println("插入排序算法：降序:"+ SortUtil.insertionSort(b,Comparator.comparing(Person::getName).reversed(),Comparator.comparing(Person::getAge).reversed()));
    System.out.println("归并排序算法：降序:"+ SortUtil.mergeSort(b,Comparator.comparing(Person::getName).reversed(),Comparator.comparing(Person::getAge).reversed()));
    System.out.println("快速排序算法：降序:"+ SortUtil.quickSort(b,Comparator.comparing(Person::getName).reversed(),Comparator.comparing(Person::getAge).reversed()));
    System.out.println("选择排序算法：降序:"+ SortUtil.selectionSort(b,Comparator.comparing(Person::getName).reversed(),Comparator.comparing(Person::getAge).reversed()));
    System.out.println("希尔排序算法：降序:"+ SortUtil.shellSort(b,Comparator.comparing(Person::getName).reversed(),Comparator.comparing(Person::getAge).reversed()));

    System.out.println("===================================");

    System.out.println("冒泡排序算法：升序:"+ SortUtil.bubbleSort(b,Comparator.comparing(Person::getName),Comparator.comparing(Person::getAge)));
    System.out.println("桶排序算法:  升序:"+ SortUtil.bucketSort(b,Comparator.comparing(Person::getName),Comparator.comparing(Person::getAge)));
    System.out.println("堆排序算法:  升序:"+ SortUtil.heapSort(b,Comparator.comparing(Person::getName),Comparator.comparing(Person::getAge)));
    System.out.println("插入排序算法：升序:"+ SortUtil.insertionSort(b,Comparator.comparing(Person::getName),Comparator.comparing(Person::getAge)));
    System.out.println("归并排序算法：升序:"+ SortUtil.mergeSort(b,Comparator.comparing(Person::getName),Comparator.comparing(Person::getAge)));
    System.out.println("快速排序算法：升序:"+ SortUtil.quickSort(b,Comparator.comparing(Person::getName),Comparator.comparing(Person::getAge)));
    System.out.println("选择排序算法：升序:"+ SortUtil.selectionSort(b,Comparator.comparing(Person::getName),Comparator.comparing(Person::getAge)));
    System.out.println("希尔排序算法：升序:"+ SortUtil.shellSort(b,Comparator.comparing(Person::getName),Comparator.comparing(Person::getAge)));
  }

  static class Person {
    private String name;
    private int age;

    public Person(String name, int age) {
      this.name = name;
      this.age = age;
    }

    public String getName() {
      return name;
    }

    public int getAge() {
      return age;
    }

    @Override
    public String toString() {
      return "Person{" +
          "name='" + name + '\'' +
          ", age=" + age +
          '}';
    }
  }
}
