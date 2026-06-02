package com.steven.solomon.datasource.sql;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL 关联片段。
 */
final class JoinSegment {

  private final JoinType joinType;
  private final String table;
  private final String alias;
  private final List<String> onConditions = new ArrayList<>();

  JoinSegment(JoinType joinType, String table, String alias) {
    this.joinType = joinType;
    this.table = table;
    this.alias = alias;
  }

  JoinSegment copy() {
    JoinSegment copy = new JoinSegment(joinType, table, alias);
    copy.onConditions.addAll(onConditions);
    return copy;
  }

  void addOn(String keyword, String condition) {
    onConditions.add(ObjectUtil.isEmpty(onConditions) ? condition : keyword + " " + condition);
  }

  String toSql() {
    StringBuilder sql = new StringBuilder(" ").append(joinType.getKeyword()).append(" ")
        .append(table);
    if (StrUtil.isNotBlank(alias)) {
      sql.append(" ").append(alias);
    }
    if (ObjectUtil.isNotEmpty(onConditions)) {
      sql.append(" ON ").append(String.join(" ", onConditions));
    }
    return sql.toString();
  }
}
