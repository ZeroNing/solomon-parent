package com.steven.solomon.datasource.sql.result;

import java.util.List;

/**
 * 分页查询结果。
 *
 * @param <T> 数据类型
 */
public class PageResult<T> {

  private final List<T> records;

  private final long total;

  private final int pageNo;

  private final int pageSize;

  private final boolean hasNext;

  private final boolean seekPage;

  private final Object nextSeekValue;

  /**
   * 构造分页结果。
   *
   * @param records 当前页记录
   * @param total 总记录数
   * @param pageNo 当前页码，从1开始
   * @param pageSize 每页条数
   */
  public PageResult(List<T> records, long total, int pageNo, int pageSize) {
    this(records, total, pageNo, pageSize, false, null);
  }

  /**
   * 构造分页结果。
   *
   * @param records 当前页记录
   * @param total 总记录数
   * @param pageNo 当前页码，从1开始
   * @param pageSize 每页条数
   * @param seekPage 是否使用了深度分页
   * @param nextSeekValue 下一页游标值；普通分页时为空
   */
  public PageResult(
      List<T> records,
      long total,
      int pageNo,
      int pageSize,
      boolean seekPage,
      Object nextSeekValue) {
    this.records = records;
    this.total = total;
    this.pageNo = pageNo;
    this.pageSize = pageSize;
    this.hasNext = (long) pageNo * pageSize < total;
    this.seekPage = seekPage;
    this.nextSeekValue = nextSeekValue;
  }

  /**
   * 获取当前页记录。
   *
   * @return 当前页记录
   */
  public List<T> getRecords() {
    return records;
  }

  /**
   * 获取总记录数。
   *
   * @return 总记录数
   */
  public long getTotal() {
    return total;
  }

  /**
   * 获取当前页码。
   *
   * @return 当前页码
   */
  public int getPageNo() {
    return pageNo;
  }

  /**
   * 获取每页条数。
   *
   * @return 每页条数
   */
  public int getPageSize() {
    return pageSize;
  }

  /**
   * 是否存在下一页。
   *
   * @return true表示存在下一页
   */
  public boolean isHasNext() {
    return hasNext;
  }

  /**
   * 是否使用了深度分页。
   *
   * @return true表示本次分页由游标深度分页生成
   */
  public boolean isSeekPage() {
    return seekPage;
  }

  /**
   * 获取下一页游标值。
   *
   * @return 下一页游标值；普通分页或无下一页时为空
   */
  public Object getNextSeekValue() {
    return nextSeekValue;
  }
}
