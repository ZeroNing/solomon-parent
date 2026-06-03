package com.steven.solomon.datasource.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.steven.solomon.datasource.exception.DataSourceException;
import com.steven.solomon.datasource.sql.SqlExecutor;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ReportQueryExecutorTest {

  @Test
  void shouldDelegateNamedParameterReportQuery() throws Exception {
    SqlExecutor sqlExecutor = mock(SqlExecutor.class);
    ReportQuery query = ReportQuery.of("select name from demo where status = :status",
        Map.of("status", 1));
    List<Map<String, Object>> expected = List.of(Map.of("name", "demo"));
    when(sqlExecutor.queryForList(query.getSql())).thenReturn(expected);

    assertEquals(expected, new ReportQueryExecutor(sqlExecutor).list(query));
  }

  @Test
  void shouldValidateAllowedParamsBeforeExecute() {
    SqlExecutor sqlExecutor = mock(SqlExecutor.class);
    ReportQuery query = ReportQuery.of("select name from demo where status = :status",
        Map.of("status", 1)).allowedParams("tenantCode");

    assertThrows(DataSourceException.class,
        () -> new ReportQueryExecutor(sqlExecutor).list(query));
  }
}
