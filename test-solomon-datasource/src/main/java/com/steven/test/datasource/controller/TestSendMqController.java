package com.steven.test.datasource.controller;

import cn.hutool.json.JSONUtil;
import com.steven.solomon.datasource.routing.DataSourceTenantContext;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.test.datasource.entity.Tenant;
import com.steven.test.datasource.repository.TenantRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class TestSendMqController {

    private final TenantRepository repository;

    private final DataSourceTenantContext context;

    public TestSendMqController(TenantRepository repository, DataSourceTenantContext context) {
        this.repository = repository;
        this.context = context;
    }

    @GetMapping("/test/{tenantCode}")
    public List<Tenant> test(@PathVariable String tenantCode) throws Exception {
        RequestHeaderHolder.setTenantCode(tenantCode);
        log.info("租户:{},租户列表JSON:{}",tenantCode,JSONUtil.toJsonStr(repository.findAll()));
        return repository.findAll();
    }
}
