package com.steven.test.persistence.repository;

import com.steven.solomon.persistence.sql.BaseRepository;
import com.steven.solomon.persistence.sql.SqlExecutor;
import com.steven.test.persistence.entity.Tenant;
import org.springframework.stereotype.Component;

@Component
public class TenantRepository extends BaseRepository<Tenant> {

    public TenantRepository(SqlExecutor sqlExecutor) {
        super(sqlExecutor);
    }


}
