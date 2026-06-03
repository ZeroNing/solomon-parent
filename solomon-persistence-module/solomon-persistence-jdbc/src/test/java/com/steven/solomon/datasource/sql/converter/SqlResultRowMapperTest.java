package com.steven.solomon.datasource.sql.converter;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class SqlResultRowMapperTest {

  @Test
  void shouldReuseResultMetadataCache() throws Exception {
    SqlResultRowMapper<UserView> first = SqlResultRowMapper.of(UserView.class);
    SqlResultRowMapper<UserView> second = SqlResultRowMapper.of(UserView.class);

    assertSame(metadata(first), metadata(second));
  }

  private static Object metadata(SqlResultRowMapper<?> mapper) throws Exception {
    Field field = SqlResultRowMapper.class.getDeclaredField("metadata");
    field.setAccessible(true);
    return field.get(mapper);
  }

  private static class UserView {

    private Long id;
    private String userName;
  }
}
