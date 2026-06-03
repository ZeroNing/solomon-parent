package com.steven.solomon.datasource.report;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.steven.solomon.datasource.exception.DataSourceException;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ReportQueryTest {

  @Test
  void shouldAllowReadonlyQuery() {
    assertDoesNotThrow(() -> ReportQuery.of("select id, name from demo where id = :id"));
  }

  @Test
  void shouldRejectWriteQuery() {
    assertThrows(DataSourceException.class,
        () -> ReportQuery.of("update demo set name = :name where id = :id"));
  }

  @Test
  void shouldAllowDeclaredReportParam() {
    ReportQuery query = ReportQuery.of("select id from demo where status = :status",
        Map.of("status", 1)).allowedParams("status");

    assertDoesNotThrow(query::validateParams);
  }

  @Test
  void shouldRejectUndeclaredReportParam() {
    ReportQuery query = ReportQuery.of("select id from demo where status = :status",
        Map.of("status", 1)).allowedParams("tenantCode");

    assertThrows(DataSourceException.class, query::validateParams);
  }
}
