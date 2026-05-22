package com.steven.test.datasource.repository;

import com.steven.solomon.datasource.sql.BaseRepository;
import com.steven.solomon.datasource.sql.SqlExecutor;
import com.steven.test.datasource.entity.Tenant;
import org.springframework.stereotype.Component;

@Component
public class TenantRepository extends BaseRepository<Tenant> {

    public TenantRepository(SqlExecutor sqlExecutor) {
        super(sqlExecutor);
    }


}
