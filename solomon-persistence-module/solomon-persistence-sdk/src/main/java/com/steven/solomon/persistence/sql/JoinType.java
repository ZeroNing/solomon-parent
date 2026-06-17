package com.steven.solomon.persistence.sql;

/**
 * SQL连表类型。
 */
public enum JoinType {
  JOIN("JOIN"),
  LEFT_JOIN("LEFT JOIN"),
  RIGHT_JOIN("RIGHT JOIN"),
  FULL_JOIN("FULL JOIN"),
  CROSS_JOIN("CROSS JOIN");

  private final String keyword;

  JoinType(String keyword) {
    this.keyword = keyword;
  }

  public String getKeyword() {
    return keyword;
  }
}
