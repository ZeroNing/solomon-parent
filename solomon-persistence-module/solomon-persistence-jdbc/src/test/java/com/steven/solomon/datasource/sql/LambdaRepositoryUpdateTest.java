package com.steven.solomon.datasource.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.steven.solomon.datasource.annotation.Column;
import com.steven.solomon.datasource.annotation.PrimaryKey;
import com.steven.solomon.datasource.annotation.Table;
import com.steven.solomon.datasource.exception.DataSourceException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class LambdaRepositoryUpdateTest {

  @Test
  void shouldBuildSafeUpdateByGetter() throws Exception {
    SqlExecutor sqlExecutor = mock(SqlExecutor.class);
    UserRepository repository = new UserRepository(sqlExecutor);
    when(sqlExecutor.update(any(Sql.class))).thenReturn(1);

    int rows = repository.lambdaUpdate()
        .set(UserEntity::getUserName, "tom")
        .eq(UserEntity::getId, 1L)
        .execute();

    ArgumentCaptor<Sql> captor = ArgumentCaptor.forClass(Sql.class);
    verify(sqlExecutor).update(captor.capture());
    Sql sql = captor.getValue();
    assertEquals(1, rows);
    String conditionParam = conditionParam(sql);
    assertEquals("UPDATE demo_user SET user_name = :set1 WHERE id = :" + conditionParam,
        sql.getText());
    assertEquals("tom", sql.getParams().get("set1"));
    assertEquals(1L, sql.getParams().get(conditionParam));
  }

  @Test
  void shouldRejectUpdateWithoutWhere() {
    SqlExecutor sqlExecutor = mock(SqlExecutor.class);
    UserRepository repository = new UserRepository(sqlExecutor);

    assertThrows(DataSourceException.class,
        () -> repository.lambdaUpdate()
            .set(UserEntity::getUserName, "tom")
            .execute());
  }

  @Test
  void shouldAppendUpdateValuesAndConditionsOnlyWhenMatched() throws Exception {
    SqlExecutor sqlExecutor = mock(SqlExecutor.class);
    UserRepository repository = new UserRepository(sqlExecutor);
    when(sqlExecutor.update(any(Sql.class))).thenReturn(1);

    repository.lambdaUpdate()
        .setIf(false, UserEntity::getId, 9L)
        .setIf(true, UserEntity::getUserName, "tom")
        .eqIf(false, UserEntity::getUserName, "skip")
        .eqIf(true, UserEntity::getId, 1L)
        .execute();

    ArgumentCaptor<Sql> captor = ArgumentCaptor.forClass(Sql.class);
    verify(sqlExecutor).update(captor.capture());
    Sql sql = captor.getValue();
    String conditionParam = conditionParam(sql);
    assertEquals("UPDATE demo_user SET user_name = :set1 WHERE id = :" + conditionParam,
        sql.getText());
    assertEquals("tom", sql.getParams().get("set1"));
    assertEquals(1L, sql.getParams().get(conditionParam));
  }

  private static class UserRepository extends Repository<UserEntity> {

    UserRepository(SqlExecutor sqlExecutor) {
      super(sqlExecutor);
    }
  }

  private static String conditionParam(Sql sql) {
    String key = sql.getParams().keySet().stream()
        .filter(item -> item.startsWith("c"))
        .findFirst()
        .orElse("");
    assertTrue(key.matches("c\\d+"));
    return key;
  }

  @Table("demo_user")
  private static class UserEntity {

    @PrimaryKey
    private Long id;

    @Column("user_name")
    private String userName;

    public Long getId() {
      return id;
    }

    public String getUserName() {
      return userName;
    }
  }
}
