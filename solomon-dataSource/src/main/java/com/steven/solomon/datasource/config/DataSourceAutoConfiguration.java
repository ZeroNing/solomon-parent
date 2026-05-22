package com.steven.solomon.datasource.config;

import com.steven.solomon.datasource.aspect.DataSourceTenantAspect;
import com.steven.solomon.datasource.code.DataSourceErrorCode;
import com.steven.solomon.datasource.exception.DataSourceException;
import com.steven.solomon.datasource.factory.DynamicDataSourceFactory;
import com.steven.solomon.datasource.properties.SolomonDataSourceProperties;
import com.steven.solomon.datasource.properties.SolomonDataSourceProperties.SingleDataSourceProperties;
import com.steven.solomon.datasource.routing.DataSourceTenantContext;
import com.steven.solomon.datasource.routing.DynamicRoutingDataSource;
import com.steven.solomon.datasource.sql.SqlExecutor;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.CollectionUtils;

/**
 * Solomon动态数据源自动配置。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SolomonDataSourceProperties.class)
@ConditionalOnProperty(prefix = "solomon.datasource", name = "enabled", havingValue = "true",
    matchIfMissing = true)
@AutoConfigureBefore(org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class)
public class DataSourceAutoConfiguration {

  private static final Logger log = LoggerFactory.getLogger(DataSourceAutoConfiguration.class);

  /**
   * 创建动态数据源工厂。
   *
   * @return 动态数据源工厂
   */
  @Bean
  @ConditionalOnMissingBean
  public DynamicDataSourceFactory dynamicDataSourceFactory() {
    return new DynamicDataSourceFactory();
  }

  /**
   * 创建数据源租户上下文。
   *
   * @return 数据源租户上下文
   */
  @Bean
  @ConditionalOnMissingBean
  public DataSourceTenantContext dataSourceTenantContext() {
    return new DataSourceTenantContext();
  }

  /**
   * 创建数据源租户切面。
   *
   * @param context 数据源租户上下文
   * @param properties 动态数据源配置
   * @return 数据源租户切面
   */
  @Bean
  @ConditionalOnMissingBean
  public DataSourceTenantAspect dataSourceTenantAspect(
      DataSourceTenantContext context,
      SolomonDataSourceProperties properties) {
    return new DataSourceTenantAspect(context, properties);
  }

  /**
   * 创建SQL执行器。
   *
   * @param jdbcTemplate Spring命名参数JDBC模板
   * @param properties 动态数据源配置
   * @param context 数据源租户上下文
   * @return SQL执行器
   */
  @Bean
  @ConditionalOnMissingBean
  public SqlExecutor sqlExecutor(
      NamedParameterJdbcTemplate jdbcTemplate,
      SolomonDataSourceProperties properties,
      DataSourceTenantContext context) {
    return new SqlExecutor(jdbcTemplate, properties, context);
  }

  /**
   * 创建Spring主数据源。
   *
   * @param properties 动态数据源配置，包含所有租户数据源
   * @param factory 数据源工厂，用于创建Hikari或Druid连接池
   * @param context 数据源租户上下文，用于注册租户数据源
   * @return 动态路由数据源
   * @throws DataSourceException 配置缺失或数据源初始化失败时抛出
   */
  @Bean
  @ConditionalOnMissingBean(DataSource.class)
  public DataSource dataSource(
      SolomonDataSourceProperties properties,
      DynamicDataSourceFactory factory,
      DataSourceTenantContext context) throws DataSourceException {
    if (CollectionUtils.isEmpty(properties.getTenants())) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_CONFIG_NOT_FOUND);
    }

    Map<Object, Object> targetDataSources = new LinkedHashMap<>();
    DataSource defaultTargetDataSource = null;
    String defaultTenant = properties.getDefaultTenant();

    for (Map.Entry<String, SingleDataSourceProperties> tenantEntry : properties.getTenants().entrySet()) {
      String tenantCode = tenantEntry.getKey();
      try {
        DataSource dataSource = factory.createDataSource(tenantEntry.getValue());
        context.registerFactory(tenantCode, dataSource);
        targetDataSources.put(tenantCode, dataSource);
        if (tenantCode.equals(defaultTenant)) {
          defaultTargetDataSource = dataSource;
        }
        log.info("[DataSource] 注册租户数据源成功: tenant={}", tenantCode);
      } catch (DataSourceException e) {
        throw e;
      } catch (Exception e) {
        throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_INIT_FAILED, e, tenantCode);
      }
    }

    if (targetDataSources.isEmpty()) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_CONFIG_NOT_FOUND);
    }
    if (defaultTargetDataSource == null) {
      defaultTargetDataSource = (DataSource) targetDataSources.values().iterator().next();
    }

    DynamicRoutingDataSource routingDataSource = new DynamicRoutingDataSource(context);
    routingDataSource.setTargetDataSources(targetDataSources);
    routingDataSource.setDefaultTargetDataSource(defaultTargetDataSource);
    routingDataSource.afterPropertiesSet();
    return routingDataSource;
  }
}
