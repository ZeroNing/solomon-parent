package com.steven.solomon.config;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.init.AbstractDataSourceInitService;
import com.steven.solomon.init.DefaultDataSourceTenantInitService;
import com.steven.solomon.properties.DataSourceProperties;
import com.steven.solomon.properties.TenantDataSourceProperties;
import com.steven.solomon.spring.SpringUtil;
import com.steven.solomon.utils.logger.LoggerUtils;
import com.steven.solomon.verification.ValidateUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Order(2)
@Import(value = {DataSourceTenantContext.class, DefaultDataSourceTenantInitService.class})
@EnableConfigurationProperties(value={TenantDataSourceProperties.class})
@ConditionalOnProperty(name = "datasource.enabled", havingValue = "true", matchIfMissing = true)
public class DataSourceConfig {

    private final Logger logger = LoggerUtils.logger(getClass());

    private final TenantDataSourceProperties properties;

    private final DataSourceTenantContext context;

    public DataSourceConfig(TenantDataSourceProperties properties, DataSourceTenantContext context, ApplicationContext applicationContext) {
        this.properties = properties;
        this.context = context;
        SpringUtil.setContext(applicationContext);
    }

    @PostConstruct
    public void afterPropertiesSet() throws Throwable {
        logger.info("数据源当前模式为:{}", properties.getMode().getDesc());
        Map<String, DataSourceProperties> tenantMap = ValidateUtils.isEmpty(properties.getTenant()) ? new HashMap<>() : properties.getTenant();
        if(!tenantMap.containsKey(BaseCode.DEFAULT)){
            tenantMap.put(BaseCode.DEFAULT, properties.getDefaultDataSource());
            properties.setTenant(tenantMap);
        }
        AbstractDataSourceInitService<DataSourceProperties,DataSourceTenantContext, DataSource> service = getService();
        service.init(properties.getTenant(),context);
    }

    private AbstractDataSourceInitService<DataSourceProperties,DataSourceTenantContext, DataSource> getService(){
        return SpringUtil.getBeansOfType(ResolvableType.forClassWithGenerics(
                AbstractDataSourceInitService.class,
                ResolvableType.forClass(DataSourceProperties.class),  // 替换P为实际类型
                ResolvableType.forClass(DataSourceTenantContext.class),  // 替换C为实际类型
                ResolvableType.forClass(DataSource.class)   // 替换F为实际类型
        ),new DefaultDataSourceTenantInitService());
    }
}
