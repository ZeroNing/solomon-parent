package com.steven.solomon.persistence.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.steven.solomon.persistence.annotation.Column;
import com.steven.solomon.persistence.annotation.PrimaryKey;
import com.steven.solomon.persistence.annotation.Table;
import com.steven.solomon.persistence.exception.PersistenceException;
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

    assertThrows(PersistenceException.class,
        () -> sql.orderByIf(true, "id DESC; DROP TABLE demo_user"));
  }

  @Test
  void shouldBuildConditionByLambdaGetter() {
    Cond cond = Cond.eq(UserEntity::getId, 123L);

    assertEquals("id = :" + cond.getParams().keySet().iterator().next(), cond.getText());
    assertEquals(123L, cond.getParams().values().iterator().next());
  }

  @Test
  void shouldBuildJoinOnByLambdaGetterWithAlias() {
    Sql sql = Sql.select("u.id", "d.dept_name")
        .from("demo_user", "u")
        .leftJoin("demo_dept", "d")
        .on(Cond.eq("u", UserEntity::getDeptId, "d", DeptEntity::getId))
        .on(Cond.eq("d", DeptEntity::getDeleted, 0));

    String deletedParam = sql.getParams().keySet().iterator().next();
    assertEquals(
        "SELECT u.id, d.dept_name FROM demo_user u LEFT JOIN demo_dept d ON u.dept_id = d.id AND d.deleted = :"
            + deletedParam,
        sql.getText());
    assertEquals(0, sql.getParams().get(deletedParam));
  }

  @Test
  void shouldBuildJoinOnByLambdaGetterWithDefaultEntityAlias() {
    Sql sql = Sql.select("demoUser.id", "demoDept.dept_name")
        .from(UserEntity.class)
        .leftJoin(DeptEntity.class)
        .on(Cond.eq(UserEntity::getDeptId, DeptEntity::getId));

    assertEquals(
        "SELECT demoUser.id, demoDept.dept_name FROM demo_user demoUser LEFT JOIN demo_dept demoDept ON demoUser.dept_id = demoDept.id",
        sql.getText());
  }

  @Table("demo_user")
  private static class UserEntity {

    @PrimaryKey
    private Long id;

    @Column("dept_id")
    private Long deptId;

    public Long getId() {
      return id;
    }

    public Long getDeptId() {
      return deptId;
    }
  }

  @Table("demo_dept")
  private static class DeptEntity {

    @PrimaryKey
    private Long id;

    @Column("dept_name")
    private String deptName;

    @Column("deleted")
    private Integer deleted;

    public Long getId() {
      return id;
    }

    public String getDeptName() {
      return deptName;
    }

    public Integer getDeleted() {
      return deleted;
    }
  }
}
