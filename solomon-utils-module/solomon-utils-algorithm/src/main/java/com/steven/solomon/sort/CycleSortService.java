package com.steven.solomon.sort;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 循环排序实现。
 *
 * <p>循环排序通过把元素放到最终位置来减少写入次数，适合写入成本较高的场景。
 * 注意这里的元素移动是“轮换”，不是普通两两交换，因此只复用集合复制逻辑。</p>
 */
public class CycleSortService implements SortService {

  @Override
  public <T> Collection<T> sort(Collection<T> list, Comparator<? super T> comparator) {
    List<T> sortedList = copyToList(list);
    for (int cycleStart = 0; cycleStart < sortedList.size() - 1; cycleStart++) {
      rotateCycle(sortedList, cycleStart, comparator);
    }
    return sortedList;
  }

  /**
   * 处理从 cycleStart 开始的一轮循环。
   */
  private <T> void rotateCycle(List<T> list, int cycleStart, Comparator<? super T> comparator) {
    T item = list.get(cycleStart);
    int position = findPosition(list, cycleStart, item, comparator);
    if (position == cycleStart) {
      return;
    }

    position = skipDuplicates(list, position, item, comparator);
    item = placeItem(list, position, item);

    while (position != cycleStart) {
      position = findPosition(list, cycleStart, item, comparator);
      position = skipDuplicates(list, position, item, comparator);
      if (comparator.compare(item, list.get(position)) != 0) {
        item = placeItem(list, position, item);
      }
    }
  }

  /**
   * 计算元素在当前轮中的目标位置。
   */
  private <T> int findPosition(List<T> list, int cycleStart, T item,
      Comparator<? super T> comparator) {
    int position = cycleStart;
    for (int i = cycleStart + 1; i < list.size(); i++) {
      if (comparator.compare(list.get(i), item) < 0) {
        position++;
      }
    }
    return position;
  }

  /**
   * 跳过重复元素，避免相同值互相覆盖。
   */
  private <T> int skipDuplicates(List<T> list, int position, T item,
      Comparator<? super T> comparator) {
    while (position < list.size() && comparator.compare(item, list.get(position)) == 0) {
      position++;
    }
    return position;
  }

  /**
   * 把 item 放到目标位置，并返回被替换出来的元素。
   */
  private <T> T placeItem(List<T> list, int position, T item) {
    T replaced = list.get(position);
    list.set(position, item);
    return replaced;
  }
}
