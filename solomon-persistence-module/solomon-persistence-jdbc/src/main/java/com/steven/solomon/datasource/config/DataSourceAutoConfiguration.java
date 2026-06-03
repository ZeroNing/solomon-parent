package com.steven.solomon.datasource.config;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.steven.solomon.datasource.aspect.DataSourceTenantAspect;
import com.steven.solomon.datasource.code.DataSourceErrorCode;
import com.steven.solomon.datasource.exception.DataSourceException;
import com.steven.solomon.datasource.factory.DynamicDataSourceFactory;
import com.steven.solomon.datasource.manager.DataSourceTenantManager;
import com.steven.solomon.datasource.properties.PersistenceProperties;
import com.steven.solomon.datasource.properties.TenantDataSourceProperties;
import com.steven.solomon.datasource.routing.DataSourceTenantContext;
import com.steven.solomon.datasource.routing.DynamicRoutingDataSource;
import com.steven.solomon.datasource.sql.SqlExecutor;
import com.steven.solomon.datasource.sql.converter.SqlTypeConverterCustomizer;
import com.steven.solomon.datasource.sql.converter.SqlTypeConverterRegistry;
import com.steven.solomon.datasource.sql.script.SqlScriptExecutor;
import com.steven.solomon.context.TenantRequestBinder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * 持久化动态数据源自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(PersistenceProperties.class)
@ConditionalOnProperty(prefix = "persistence", name = "enabled", havingValue = "true",
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
      PersistenceProperties properties) {
    return new DataSourceTenantAspect(context, properties);
  }

  /**
   * 将网关透传的租户编码绑定到当前请求的数据源上下文。
   */
  @Bean
  public TenantRequestBinder dataSourceTenantRequestBinder(DataSourceTenantContext context) {
    return new DataSourceTenantRequestBinder(context);
  }

  /**
   * 创建SQL类型转换器注册器。
   *
   * <p>默认注册器内置常见时间、数字、枚举等类型转换；业务模块可以声明
   * {@link SqlTypeConverterCustomizer} Bean，通过 {@code registry.addConverter(...)}
   * 追加自定义转换器。</p>
   *
   * @param customizers 业务侧提供的转换器自定义回调集合
   * @return SQL类型转换器注册器
   */
  @Bean
  @ConditionalOnMissingBean
  public SqlTypeConverterRegistry sqlTypeConverterRegistry(
      List<SqlTypeConverterCustomizer> customizers) {
    SqlTypeConverterRegistry registry = SqlTypeConverterRegistry.defaultRegistry();
    if (ObjectUtil.isNotEmpty(customizers)) {
      for (SqlTypeConverterCustomizer customizer : customizers) {
        customizer.customize(registry);
      }
    }
    return registry;
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
      PersistenceProperties properties,
      DataSourceTenantContext context,
      SqlTypeConverterRegistry converterRegistry) {
    return new SqlExecutor(jdbcTemplate, properties, context, converterRegistry);
  }

  /**
   * 创建SQL脚本执行工具。
   *
   * @param jdbcTemplate Spring命名参数JDBC模板
   * @param properties 动态数据源配置
   * @param context 数据源租户上下文
   * @return SQL脚本执行工具
   */
  @Bean
  @ConditionalOnMissingBean
  public SqlScriptExecutor sqlScriptExecutor(
      NamedParameterJdbcTemplate jdbcTemplate,
      PersistenceProperties properties,
      DataSourceTenantContext context) {
    return new SqlScriptExecutor(jdbcTemplate, properties, context);
  }

  /**
   * 创建数据源租户运行时管理器。
   *
   * @param properties 动态数据源配置
   * @param factory 数据源工厂
   * @param context 数据源租户上下文
   * @return 数据源租户运行时管理器
   */
  @Bean
  @ConditionalOnMissingBean
  public DataSourceTenantManager dataSourceTenantManager(
      PersistenceProperties properties,
      DynamicDataSourceFactory factory,
      DataSourceTenantContext context) {
    return new DataSourceTenantManager(properties, factory, context);
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
      PersistenceProperties properties,
      DynamicDataSourceFactory factory,
      DataSourceTenantContext context) throws DataSourceException {
    if (ObjectUtil.isEmpty(properties.getTenants())) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_CONFIG_NOT_FOUND);
    }

    Map<Object, Object> targetDataSources = new LinkedHashMap<>();
    DataSource defaultTargetDataSource = null;
    String defaultTenant = properties.getDefaultTenant();

    for (Map.Entry<String, TenantDataSourceProperties> tenantEntry : properties.getTenants().entrySet()) {
      String tenantCode = tenantEntry.getKey();
      if (StrUtil.isBlank(tenantCode) || ObjectUtil.isEmpty(tenantEntry.getValue())) {
        throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_CONFIG_NOT_FOUND,
            tenantCode);
      }
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

    if (ObjectUtil.isEmpty(targetDataSources)) {
      throw new DataSourceException(DataSourceErrorCode.DATA_SOURCE_CONFIG_NOT_FOUND);
    }
    if (ObjectUtil.isEmpty(defaultTargetDataSource)) {
      defaultTargetDataSource = (DataSource) targetDataSources.values().iterator().next();
      log.warn("[DataSource] 未找到默认租户数据源 defaultTenant={}，已回退到第一个可用数据源",
          defaultTenant);
    }

    DynamicRoutingDataSource routingDataSource = new DynamicRoutingDataSource(context);
    routingDataSource.setTargetDataSources(targetDataSources);
    routingDataSource.setDefaultTargetDataSource(defaultTargetDataSource);
    routingDataSource.afterPropertiesSet();
    return routingDataSource;
  }
}
