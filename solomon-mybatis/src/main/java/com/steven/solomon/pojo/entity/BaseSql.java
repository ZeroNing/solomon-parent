package com.steven.solomon.pojo.entity;

import cn.hutool.core.util.StrUtil;

import java.io.Serializable;

public abstract class BaseSql implements Serializable {

  protected StringBuilder sql;

  public BaseSql() {
    this.sql = new StringBuilder();
  }

  public BaseSql(String sql){
    this();
    this.sql = new StringBuilder();
    this.sql = this.sql.append(sql);
  }

  public StringBuilder getSql() {
    return sql;
  }

  public void setSql(StringBuilder sql) {
    this.sql = sql;
  }

  @Override
  public String toString() {
    return StrUtil.toString(sql);
  }
}
