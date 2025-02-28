package com.steven.solomon.service;


import com.steven.solomon.properties.DataSourceProperties;

import javax.sql.DataSource;

public interface DataSourceService {

    DataSource getDataSource(DataSourceProperties properties) throws Exception;
}
