package com.steven.solomon.persistence.sql.param;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.persistence.sql.SqlInjectionGuard;
import com.steven.solomon.pojo.enums.OrderByEnum;
import java.io.Serializable;

/**
 * 分页排序字段�? */
public class Sort implements Serializable {

  private static final long serialVersionUID = -8296450573572322078L;

  private String orderByField;

  private OrderByEnum orderByMethod = OrderByEnum.DESCEND;

  public Sort() {
  }

  public Sort(String orderByField, OrderByEnum orderByMethod) {
    this.orderByField = orderByField;
    this.orderByMethod = orderByMethod;
  }

  public String getSort() {
    if (StrUtil.isBlank(orderByField)) {
      return "";
    }
    SqlInjectionGuard.validateExpression(orderByField, "pageOrderByField");
    return orderByField + " " + ObjectUtil.defaultIfNull(orderByMethod, OrderByEnum.DESCEND).label();
  }

  public String getOrderByField() {
    return orderByField;
  }

  public void setOrderByField(String orderByField) {
    this.orderByField = orderByField;
  }

  public OrderByEnum getOrderByMethod() {
    return orderByMethod;
  }

  public void setOrderByMethod(OrderByEnum orderByMethod) {
    this.orderByMethod = orderByMethod;
  }
}
