package com.steven.solomon.datasource.sql.param;

import com.steven.solomon.pojo.enums.OrderByEnum;
import com.steven.solomon.datasource.sql.SqlInjectionGuard;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 数据源基础分页请求参数。
 *
 * <p>用于普通分页查询，包含页码、页大小、是否分页和排序字段。排序字段会直接拼接到SQL，
 * 建议只接收后端白名单字段，避免前端传入任意SQL片段。</p>
 */
public class DataSourcePageParam implements Serializable {

  private static final long serialVersionUID = 5166884989506607193L;

  /** 默认第一页。 */
  private int pageNo = 1;

  /** 默认每页10条。 */
  private int pageSize = 10;

  /** 是否分页；false时调用方可选择直接查询列表。 */
  private boolean page = true;

  /** 排序字段集合。 */
  private List<Sort> sorted = new ArrayList<>();

  /** 原始排序表达式，适合后端白名单字段转换后的直接排序。 */
  private String orderBy;

  /** 游标字段；触发深度分页时使用，未设置时读取配置里的默认游标字段。 */
  private String seekColumn;

  /** 上一页最后一条记录的游标值；为空且页码较大时框架会先查询锚点游标。 */
  private Object lastValue;

  /** 游标分页是否按升序推进。 */
  private boolean asc = true;

  /** 是否允许该分页请求自动切换为深度分页。 */
  private boolean seek = true;

  /**
   * 创建分页参数。
   *
   * @param pageNo 页码，从1开始
   * @param pageSize 每页条数
   * @return 分页参数对象
   */
  public static DataSourcePageParam of(int pageNo, int pageSize) {
    DataSourcePageParam param = new DataSourcePageParam();
    param.setPageNo(pageNo);
    param.setPageSize(pageSize);
    return param;
  }

  /**
   * 获取页码。
   *
   * @return 页码，从1开始
   */
  public int getPageNo() {
    return pageNo;
  }

  /**
   * 设置页码。
   *
   * @param pageNo 页码，从1开始；小于1时执行层会按1处理
   */
  public void setPageNo(int pageNo) {
    this.pageNo = pageNo;
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
   * 设置每页条数。
   *
   * @param pageSize 每页条数；小于1时执行层会按1处理
   */
  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  /**
   * 是否分页。
   *
   * @return true表示分页，false表示不分页
   */
  public boolean isPage() {
    return page;
  }

  /**
   * 设置是否分页。
   *
   * @param page true表示分页，false表示不分页
   */
  public void setPage(boolean page) {
    this.page = page;
  }

  /**
   * 获取排序字段集合。
   *
   * @return 排序字段集合
   */
  public List<Sort> getSorted() {
    return sorted;
  }

  /**
   * 设置排序字段集合。
   *
   * @param sorted 排序字段集合；为空时不拼接ORDER BY
   */
  public void setSorted(List<Sort> sorted) {
    this.sorted = sorted;
  }

  /**
   * 获取原始排序表达式。
   *
   * @return 排序表达式，不包含 {@code ORDER BY}
   */
  public String getOrderBy() {
    return orderBy;
  }

  /**
   * 设置原始排序表达式。
   *
   * @param orderBy 排序表达式，不包含 {@code ORDER BY}，例如 {@code "id DESC"}
   */
  public void setOrderBy(String orderBy) {
    if (StringUtils.hasText(orderBy)) {
      SqlInjectionGuard.validateOrderBy(orderBy, "pageOrderBy");
    }
    this.orderBy = orderBy;
  }

  /**
   * 链式设置原始排序表达式。
   *
   * @param orderBy 排序表达式，不包含 {@code ORDER BY}
   * @return 当前分页参数
   */
  public DataSourcePageParam orderBy(String orderBy) {
    setOrderBy(orderBy);
    return this;
  }

  /**
   * 追加排序字段。
   *
   * @param field 排序字段名或表达式
   * @param method 排序方向；为空时默认倒序
   * @return 当前分页参数
   */
  public DataSourcePageParam sort(String field, OrderByEnum method) {
    this.sorted.add(new Sort(field, method));
    return this;
  }

  /**
   * 追加升序排序字段。
   *
   * @param field 排序字段名或表达式
   * @return 当前分页参数
   */
  public DataSourcePageParam asc(String field) {
    return sort(field, OrderByEnum.ASC);
  }

  /**
   * 追加降序排序字段。
   *
   * @param field 排序字段名或表达式
   * @return 当前分页参数
   */
  public DataSourcePageParam desc(String field) {
    return sort(field, OrderByEnum.DESCEND);
  }

  /**
   * 获取游标字段。
   *
   * @return 游标字段名或表达式，例如 {@code "u.id"}
   */
  public String getSeekColumn() {
    return seekColumn;
  }

  /**
   * 设置游标字段。
   *
   * @param seekColumn 游标字段名或表达式，建议使用有索引且排序稳定的字段
   */
  public void setSeekColumn(String seekColumn) {
    this.seekColumn = seekColumn;
  }

  /**
   * 链式设置游标字段。
   *
   * @param seekColumn 游标字段名或表达式
   * @return 当前分页参数
   */
  public DataSourcePageParam seekColumn(String seekColumn) {
    setSeekColumn(seekColumn);
    return this;
  }

  /**
   * 获取上一页最后一条记录的游标值。
   *
   * @return 游标值；第一页或自动锚点分页时可以为空
   */
  public Object getLastValue() {
    return lastValue;
  }

  /**
   * 设置上一页最后一条记录的游标值。
   *
   * @param lastValue 游标值；为空时框架会在大页码场景下自动查询锚点
   */
  public void setLastValue(Object lastValue) {
    this.lastValue = lastValue;
  }

  /**
   * 链式设置上一页最后一条记录的游标值。
   *
   * @param lastValue 游标值
   * @return 当前分页参数
   */
  public DataSourcePageParam lastValue(Object lastValue) {
    setLastValue(lastValue);
    return this;
  }

  /**
   * 是否按升序游标分页。
   *
   * @return true表示升序，false表示降序
   */
  public boolean isAsc() {
    return asc;
  }

  /**
   * 设置是否按升序游标分页。
   *
   * @param asc true表示升序并使用大于游标值，false表示降序并使用小于游标值
   */
  public void setAsc(boolean asc) {
    this.asc = asc;
  }

  /**
   * 链式设置升序游标分页。
   *
   * @return 当前分页参数
   */
  public DataSourcePageParam seekAsc() {
    this.asc = true;
    return this;
  }

  /**
   * 链式设置降序游标分页。
   *
   * @return 当前分页参数
   */
  public DataSourcePageParam seekDesc() {
    this.asc = false;
    return this;
  }

  /**
   * 是否允许自动切换为深度分页。
   *
   * @return true表示允许，false表示始终使用普通OFFSET分页
   */
  public boolean isSeek() {
    return seek;
  }

  /**
   * 设置是否允许自动切换为深度分页。
   *
   * @param seek true表示允许，false表示始终使用普通OFFSET分页
   */
  public void setSeek(boolean seek) {
    this.seek = seek;
  }

  /**
   * 生成ORDER BY后面的排序表达式。
   *
   * @return 排序表达式，例如 {@code "id DESC,create_time ASC"}；无排序时返回空字符串
   */
  public String orderBy() {
    if (StringUtils.hasText(orderBy)) {
      SqlInjectionGuard.validateOrderBy(orderBy, "pageOrderBy");
      return orderBy;
    }
    if (CollectionUtils.isEmpty(sorted)) {
      return "";
    }
    StringBuilder builder = new StringBuilder();
    for (Sort sort : sorted) {
      String value = sort.getSort();
      if (StringUtils.hasText(value)) {
        SqlInjectionGuard.validateOrderBy(value, "pageOrderBy");
        builder.append(value).append(",");
      }
    }
    if (builder.isEmpty()) {
      return "";
    }
    return builder.substring(0, builder.length() - 1);
  }

  /**
   * 分页排序字段。
   */
  public static class Sort implements Serializable {

    private static final long serialVersionUID = -8296450573572322078L;

    /** 排序字段，建议使用后端白名单字段。 */
    private String orderByField;

    /** 排序方向。 */
    private OrderByEnum orderByMethod = OrderByEnum.DESCEND;

    /**
     * 构造排序字段。
     */
    public Sort() {
    }

    /**
     * 构造排序字段。
     *
     * @param orderByField 排序字段名或表达式
     * @param orderByMethod 排序方向
     */
    public Sort(String orderByField, OrderByEnum orderByMethod) {
      this.orderByField = orderByField;
      this.orderByMethod = orderByMethod;
    }

    /**
     * 生成单个排序表达式。
     *
     * @return 排序表达式，例如 {@code "id DESC"}；字段为空时返回空字符串
     */
    public String getSort() {
      if (!StringUtils.hasText(orderByField)) {
        return "";
      }
      SqlInjectionGuard.validateExpression(orderByField, "pageOrderByField");
      OrderByEnum method = orderByMethod == null ? OrderByEnum.DESCEND : orderByMethod;
      return orderByField + " " + method.label();
    }

    /**
     * 获取排序字段。
     *
     * @return 排序字段名或表达式
     */
    public String getOrderByField() {
      return orderByField;
    }

    /**
     * 设置排序字段。
     *
     * @param orderByField 排序字段名或表达式
     */
    public void setOrderByField(String orderByField) {
      this.orderByField = orderByField;
    }

    /**
     * 获取排序方向。
     *
     * @return 排序方向
     */
    public OrderByEnum getOrderByMethod() {
      return orderByMethod;
    }

    /**
     * 设置排序方向。
     *
     * @param orderByMethod 排序方向
     */
    public void setOrderByMethod(OrderByEnum orderByMethod) {
      this.orderByMethod = orderByMethod;
    }
  }
}
