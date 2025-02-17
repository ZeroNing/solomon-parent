package com.steven.solomon.properties;

import com.steven.solomon.pojo.enums.SwitchModeEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;

import java.util.Map;

@ConfigurationProperties(
        prefix = "datasource"
)
public class TenantDataSourceProperties {

    private Map<String, DataSourceProperties> tenant;

    private SwitchModeEnum mode = SwitchModeEnum.NORMAL;

    private DataSourceProperties defaultDataSource;

    public DataSourceProperties getDefaultDataSource() {
        return defaultDataSource;
    }

    public void setDefaultDataSource(DataSourceProperties defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    public Map<String, DataSourceProperties> getTenant() {
        return tenant;
    }

    public void setTenant(Map<String, DataSourceProperties> tenant) {
        this.tenant = tenant;
    }

    public SwitchModeEnum getMode() {
        return mode;
    }

    public void setMode(SwitchModeEnum mode) {
        this.mode = mode;
    }
}
