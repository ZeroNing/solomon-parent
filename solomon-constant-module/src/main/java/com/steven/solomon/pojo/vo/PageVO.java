package com.steven.solomon.pojo.vo;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回对象。
 *
 * <p>由列表数据、总数和当前分页参数计算是否存在下一页。</p>
 */
public class PageVO<T> implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 总记录数。
   */
  private long total;

  /**
   * 当前页码。
   */
  private int pageNo;

  /**
   * 每页条数。
   */
  private int pageSize;

  /**
   * 是否还有下一页。
   */
  private boolean next;

  /**
   * 当前页数据。
   */
  private List<T> data;

  /**
   * 构造分页响应对象。
   *
   * @param data     当前页数据列表
   * @param total    总记录数
   * @param pageNo   当前页码
   * @param pageSize 每页条数
   */
  public PageVO(List<T> data, long total, int pageNo, int pageSize) {
    this.total = total;
    this.pageNo = pageNo;
    this.pageSize = pageSize;
    this.data = data;
    this.next = hasNext(pageNo, pageSize, total);
  }

  /**
   * 判断是否存在下一页。
   *
   * @param pageNo   当前页码
   * @param pageSize 每页条数
   * @param total    总记录数
   * @return true 表示还有下一页
   */
  private boolean hasNext(int pageNo, int pageSize, long total) {
    return (long) pageNo * pageSize < total;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public int getPageNo() {
    return pageNo;
  }

  public void setPageNo(int pageNo) {
    this.pageNo = pageNo;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public boolean isNext() {
    return next;
  }

  public void setNext(boolean next) {
    this.next = next;
  }

  public List<T> getData() {
    return data;
  }

  public void setData(List<T> data) {
    this.data = data;
  }
}
