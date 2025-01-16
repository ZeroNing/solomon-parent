package com.steven.solomon.service;

import com.steven.solomon.context.TenantContext;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;

import java.util.Map;

public abstract class AbstractTenantInitService<P,C extends TenantContext<?>,F> {

    protected final Logger logger = LoggerUtils.logger(getClass());

    public void init(Map<String, P> propertiesMap, C context) {
        for(Map.Entry<String,P> entry : propertiesMap.entrySet()){
            init(entry.getKey(),entry.getValue(),context);
        }
    }
    /**
     * 初始化单个租户组件
     * @param tenantCode 租户编码
     * @param properties 单个租户组件配置
     * @param context 租户切换类
     */
    public abstract void init(String tenantCode, P properties, C context);

    /**
     * 初始化工厂/数据库 dataSource
     * @param properties 单个租户组件配置
     * @return 工厂类/数据库 dataSource
     */
    public abstract F initFactory(P properties);
}
