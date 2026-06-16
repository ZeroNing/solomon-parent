package com.steven.solomon.persistence.sql;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.steven.solomon.persistence.annotation.Column;
import com.steven.solomon.persistence.annotation.PrimaryKey;
import com.steven.solomon.persistence.annotation.Table;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class RepositoryConvenienceTest {

  @Test
  void shouldCheckExistsById() throws Exception {
    SqlExecutor sqlExecutor = mock(SqlExecutor.class);
    UserRepository repository = new UserRepository(sqlExecutor);
    when(sqlExecutor.count(any(Sql.class))).thenReturn(1L);

    assertTrue(repository.existsById(1L));

    ArgumentCaptor<Sql> captor = ArgumentCaptor.forClass(Sql.class);
    verify(sqlExecutor).count(captor.capture());
    Sql sql = captor.getValue();
    assertEquals("SELECT * FROM demo_user WHERE id = :p1", sql.getText());
    assertEquals(1L, sql.getParams().get("p1"));
  }

  @Test
  void shouldDeleteByIdsWithNamedParameter() throws Exception {
    SqlExecutor sqlExecutor = mock(SqlExecutor.class);
    UserRepository repository = new UserRepository(sqlExecutor);
    when(sqlExecutor.update(any(Sql.class))).thenReturn(2);

    int rows = repository.deleteByIds(List.of(1L, 2L));

    ArgumentCaptor<Sql> captor = ArgumentCaptor.forClass(Sql.class);
    verify(sqlExecutor).update(captor.capture());
    Sql sql = captor.getValue();
    assertEquals(2, rows);
    assertEquals("DELETE FROM demo_user WHERE id IN (:ids)", sql.getText());
    assertEquals(List.of(1L, 2L), sql.getParams().get("ids"));
  }

  @Test
  void shouldUpdateColumnsByGetter() throws Exception {
    SqlExecutor sqlExecutor = mock(SqlExecutor.class);
    UserRepository repository = new UserRepository(sqlExecutor);
    when(sqlExecutor.batchUpdate(eq(
        "UPDATE demo_user SET user_name = :userName WHERE id = :id"), any()))
        .thenReturn(new int[] {1});

    int rows = repository.updateColumns(new UserEntity(1L, "tom", 18), UserEntity::getUserName);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Map<String, ?>[]> captor = ArgumentCaptor.forClass(Map[].class);
    verify(sqlExecutor).batchUpdate(eq(
        "UPDATE demo_user SET user_name = :userName WHERE id = :id"), captor.capture());
    assertEquals(1, rows);
    assertArrayEquals(new int[] {1}, new int[] {rows});
    assertEquals("tom", captor.getValue()[0].get("userName"));
    assertEquals(1L, captor.getValue()[0].get("id"));
  }

  private static class UserRepository extends Repository<UserEntity> {

    UserRepository(SqlExecutor sqlExecutor) {
      super(sqlExecutor);
    }
  }

  @Table("demo_user")
  private static class UserEntity {

    @PrimaryKey
    private final Long id;

    @Column("user_name")
    private final String userName;

    @Column("age")
    private final Integer age;

    UserEntity(Long id, String userName, Integer age) {
      this.id = id;
      this.userName = userName;
      this.age = age;
    }

    public Long getId() {
      return id;
    }

    public String getUserName() {
      return userName;
    }

    public Integer getAge() {
      return age;
    }
  }
}
