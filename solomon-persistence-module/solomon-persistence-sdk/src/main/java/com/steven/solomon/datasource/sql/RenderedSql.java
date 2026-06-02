package com.steven.solomon.datasource.sql;

import java.util.Map;

/**
 * 渲染后的 SQL 与命名参数。
 */
record RenderedSql(String text, Map<String, Object> params) {
}
