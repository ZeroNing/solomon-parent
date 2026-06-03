package com.steven.solomon.datasource.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.steven.solomon.datasource.exception.DataSourceException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SqlBuilderTest {

  @Test
  void shouldAppendDynamicConditionsOnlyWhenMatched() {
    Sql sql = Sql.select("id", "name")
        .from("demo_user")
        .eqIf(false, "id", 1L)
        .likeIf(true, "name", "%tom%")
        .inIf(true, "status", List.of(1, 2))
        .orderByIf(false, "name ASC")
        .orderByIf(true, "id DESC");

    assertEquals(
        "SELECT id, name FROM demo_user WHERE name LIKE :p1 AND status IN (:p2) ORDER BY id DESC",
        sql.getText());
    assertEquals("%tom%", sql.getParams().get("p1"));
    assertEquals(List.of(1, 2), sql.getParams().get("p2"));
  }

  @Test
  void shouldBindParamsForRawWhereAndHaving() {
    Sql sql = Sql.select("status", "COUNT(1) AS total")
        .from("demo_order")
        .where("tenant_code = :tenantCode", Map.of("tenantCode", "t1"))
        .groupBy("status")
        .having("COUNT(1) > :minTotal", Map.of("minTotal", 10));

    assertEquals(
        "SELECT status, COUNT(1) AS total FROM demo_order WHERE tenant_code = :tenantCode GROUP BY status HAVING COUNT(1) > :minTotal",
        sql.getText());
    assertEquals("t1", sql.getParams().get("tenantCode"));
    assertEquals(10, sql.getParams().get("minTotal"));
  }

  @Test
  void shouldRejectDangerousDynamicOrderBy() {
    Sql sql = Sql.select("*").from("demo_user");

    assertThrows(DataSourceException.class,
        () -> sql.orderByIf(true, "id DESC; DROP TABLE demo_user"));
  }
}
