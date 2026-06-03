package com.steven.solomon.datasource.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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

class RepositoryProjectionTest {

  @Test
  void shouldQueryProjectionFromRepositoryQuery() throws Exception {
    SqlExecutor sqlExecutor = mock(SqlExecutor.class);
    UserRepository repository = new UserRepository(sqlExecutor);
    List<UserView> expected = List.of(new UserView());
    when(sqlExecutor.query(any(Sql.class), eq(UserView.class))).thenReturn(expected);

    List<UserView> actual = repository.query(Sql.select("id", "user_name").from("demo_user"))
        .list(UserView.class);

    ArgumentCaptor<Sql> captor = ArgumentCaptor.forClass(Sql.class);
    verify(sqlExecutor).query(captor.capture(), eq(UserView.class));
    assertEquals(expected, actual);
    assertEquals("SELECT id, user_name FROM demo_user", captor.getValue().getText());
  }

  @Test
  void shouldQueryProjectionFromLambdaQuery() throws Exception {
    SqlExecutor sqlExecutor = mock(SqlExecutor.class);
    UserRepository repository = new UserRepository(sqlExecutor);
    List<UserView> expected = List.of(new UserView());
    when(sqlExecutor.query(any(Sql.class), eq(UserView.class))).thenReturn(expected);

    List<UserView> actual = repository.lambdaQuery(UserEntity::getId, UserEntity::getUserName)
        .list(UserView.class);

    ArgumentCaptor<Sql> captor = ArgumentCaptor.forClass(Sql.class);
    verify(sqlExecutor).query(captor.capture(), eq(UserView.class));
    assertEquals(expected, actual);
    assertEquals("SELECT id, user_name FROM demo_user", captor.getValue().getText());
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

  private static class UserView {

    private Long id;
    private String userName;
  }
}
