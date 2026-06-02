package com.steven.solomon.datasource.sql;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.steven.solomon.datasource.enums.DataBaseTypeEnum;
import org.junit.jupiter.api.Test;

class SqlDialectFactoryTest {

  @Test
  void shouldResolveDialectForEverySupportedDatabase() {
    for (DataBaseTypeEnum databaseType : DataBaseTypeEnum.values()) {
      assertNotNull(SqlDialectFactory.getDialect(databaseType));
      assertFalse(SqlDialectFactory.getDialect(databaseType)
          .pageSql("select * from demo order by id", 2, 10)
          .isBlank());
    }
  }
}
