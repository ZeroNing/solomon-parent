package com.steven.solomon.datasource.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.steven.solomon.datasource.annotation.Column;
import com.steven.solomon.datasource.annotation.PrimaryKey;
import com.steven.solomon.datasource.annotation.Table;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class LambdaRepositoryQueryTest {

  @Test
  void shouldResolveGetterToColumnName() throws Exception {
    SqlExecutor sqlExecutor = mock(SqlExecutor.class);
    UserRepository repository = new UserRepository(sqlExecutor);
    when(sqlExecutor.query(org.mockito.ArgumentMatchers.any(Sql.class), eq(UserEntity.class)))
        .thenReturn(List.of());

    repository.lambdaQuery()
        .eq(UserEntity::getUserName, "tom")
        .orderByDesc(UserEntity::getId)
        .list();

    ArgumentCaptor<Sql> captor = ArgumentCaptor.forClass(Sql.class);
    verify(sqlExecutor).query(captor.capture(), eq(UserEntity.class));
    Sql sql = captor.getValue();
    assertEquals("SELECT * FROM demo_user WHERE user_name = :p1 ORDER BY id DESC",
        sql.getText());
    assertEquals("tom", sql.getParams().get("p1"));
  }

  @Test
  void shouldSelectColumnsByGetter() throws Exception {
    SqlExecutor sqlExecutor = mock(SqlExecutor.class);
    UserRepository repository = new UserRepository(sqlExecutor);
    when(sqlExecutor.query(org.mockito.ArgumentMatchers.any(Sql.class), eq(UserEntity.class)))
        .thenReturn(List.of());

    repository.lambdaQuery(UserEntity::getId, UserEntity::getUserName)
        .isNotNull(UserEntity::getUserName)
        .list();

    ArgumentCaptor<Sql> captor = ArgumentCaptor.forClass(Sql.class);
    verify(sqlExecutor).query(captor.capture(), eq(UserEntity.class));
    assertEquals("SELECT id, user_name FROM demo_user WHERE user_name IS NOT NULL",
        captor.getValue().getText());
  }

  @Test
  void shouldAppendQueryConditionOnlyWhenMatched() throws Exception {
    SqlExecutor sqlExecutor = mock(SqlExecutor.class);
    UserRepository repository = new UserRepository(sqlExecutor);
    when(sqlExecutor.query(org.mockito.ArgumentMatchers.any(Sql.class), eq(UserEntity.class)))
        .thenReturn(List.of());

    repository.lambdaQuery()
        .eqIf(false, UserEntity::getId, 1L)
        .likeIf(true, UserEntity::getUserName, "%tom%")
        .orderByAscIf(false, UserEntity::getId)
        .orderByDescIf(true, UserEntity::getId)
        .list();

    ArgumentCaptor<Sql> captor = ArgumentCaptor.forClass(Sql.class);
    verify(sqlExecutor).query(captor.capture(), eq(UserEntity.class));
    Sql sql = captor.getValue();
    assertEquals("SELECT * FROM demo_user WHERE user_name LIKE :p1 ORDER BY id DESC",
        sql.getText());
    assertEquals("%tom%", sql.getParams().get("p1"));
  }

  private static class UserRepository extends Repository<UserEntity> {

    UserRepository(SqlExecutor sqlExecutor) {
      super(sqlExecutor);
    }
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
