package com.steven.solomon.datasource.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.steven.solomon.datasource.annotation.Column;
import com.steven.solomon.datasource.annotation.PrimaryKey;
import com.steven.solomon.datasource.annotation.Table;
import org.junit.jupiter.api.Test;

class LambdaPageParamTest {

  @Test
  void shouldResolveSortAndSeekColumnByGetter() throws Exception {
    UserRepository repository = new UserRepository(mock(SqlExecutor.class));

    LambdaPageParam<UserEntity> param = repository.pageParam(2, 20)
        .desc(UserEntity::getUserName)
        .asc(UserEntity::getId)
        .seekColumn(UserEntity::getId)
        .seekDesc()
        .lastValue(100L);

    assertEquals(2, param.getPageNo());
    assertEquals(20, param.getPageSize());
    assertEquals("user_name DESC,id ASC", param.orderBy());
    assertEquals("id", param.getSeekColumn());
    assertEquals(100L, param.getLastValue());
    assertEquals(false, param.getAsc());
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
