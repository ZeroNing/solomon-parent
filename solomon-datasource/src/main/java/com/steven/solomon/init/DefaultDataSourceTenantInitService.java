package com.steven.solomon.init;

import com.steven.solomon.code.SqlErrorCode;
import com.steven.solomon.config.DataSourceTenantContext;
import com.steven.solomon.exception.BaseException;
import com.steven.solomon.pojo.enums.ConnectionPoolTypeEnum;
import com.steven.solomon.properties.DataSourceProperties;
import com.steven.solomon.service.impl.DruidDataSourceService;
import com.steven.solomon.service.impl.HikariCPDataSourceService;
import com.steven.solomon.utils.i18n.I18nUtils;
import com.steven.solomon.verification.ValidateUtils;

import javax.sql.DataSource;

/**
 * 默认数据源租户初始化服务
 * 
 * <h2>功能说明</h2>
 * <p>负责为多租户环境创建和管理数据源连接池</p>
 * 
 * <h2>支持的连接池类型</h2>
 * <ul>
 *   <li><b>DRUID</b> - 阿里巴巴Druid连接池，提供强大的监控和扩展功能</li>
 *   <li><b>HIKARICP</b> - 高性能连接池，Spring Boot默认连接池</li>
 * </ul>
 * 
 * <h2>使用示例</h2>
 * <pre>{@code
 * DefaultDataSourceTenantInitService initService = new DefaultDataSourceTenantInitService();
 * DataSourceProperties properties = new DataSourceProperties();
 * properties.setConnectionPoolType(ConnectionPoolTypeEnum.DRUID);
 * properties.setUrl("jdbc:mysql://localhost:3306/mydb");
 * initService.init("tenant-001", properties, context);
 * }</pre>
 * 
 * @author steven
 * @since 1.0.0
 * @see ConnectionPoolTypeEnum
 * @see DruidDataSourceService
 * @see HikariCPDataSourceService
 */
public class DefaultDataSourceTenantInitService extends AbstractDataSourceInitService<DataSourceProperties, DataSourceTenantContext, DataSource>{

    /**
     * 初始化租户数据源
     * 
     * <p>流程：</p>
     * <ol>
     *   <li>根据配置创建数据源连接池</li>
     *   <li>将数据源注册到租户上下文</li>
     * </ol>
     * 
     * @param tenantCode 租户编码（唯一标识）
     * @param properties 数据源配置（URL、用户名、密码、连接池类型等）
     * @param context 租户上下文（用于存储数据源映射）
     * @throws Throwable 数据源创建失败时抛出
     */
    @Override
    public void init(String tenantCode, DataSourceProperties properties, DataSourceTenantContext context) throws Throwable {
        log.info("[DataSource] 开始初始化租户数据源: tenantCode={}, poolType={}", 
            tenantCode, properties.getConnectionPoolType());
        
        long startTime = System.currentTimeMillis();
        try {
            // 创建数据源连接池
            DataSource dataSource = initFactory(properties);
            
            // 注册到租户上下文
            context.registerFactory(tenantCode, dataSource);
            
            long cost = System.currentTimeMillis() - startTime;
            log.info("[DataSource] 租户数据源初始化成功: tenantCode={}, poolType={}, cost={}ms", 
                tenantCode, properties.getConnectionPoolType(), cost);
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - startTime;
            log.error("[DataSource] 租户数据源初始化失败: tenantCode={}, poolType={}, cost={}ms, error={}", 
                tenantCode, properties.getConnectionPoolType(), cost, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 创建数据源连接池
     * 
     * <h3>连接池选择逻辑</h3>
     * <ul>
     *   <li>如果连接池类型为空，默认使用DRUID</li>
     *   <li>根据类型创建对应的数据源服务</li>
     * </ul>
     * 
     * <h3>⚠️ Switch穿透Bug修复</h3>
     * <p>原Bug：每个case缺少break语句，导致：</p>
     * <ul>
     *   <li>DRUID case会穿透到HIKARICP，覆盖dataSource</li>
     *   <li>最终穿透到default，抛出异常</li>
     * </ul>
     * 
     * @param properties 数据源配置
     * @return 配置好的数据源实例
     * @throws BaseException 当连接池类型不匹配时抛出
     */
    @Override
    public DataSource initFactory(DataSourceProperties properties) throws Throwable {
        // Step 1: 获取连接池类型，默认DRUID
        ConnectionPoolTypeEnum connectionPool = properties.getConnectionPoolType();
        if(ValidateUtils.isEmpty(connectionPool)){
            log.info("[DataSource] 连接池类型为空，自动默认为Druid");
            connectionPool = ConnectionPoolTypeEnum.DRUID;
        }
        
        log.debug("[DataSource] 开始创建连接池: poolType={}, url={}", 
            connectionPool, properties.getUrl());
        
        DataSource dataSource = null;
        long startTime = System.currentTimeMillis();
        
        // Step 2: 根据类型创建数据源
        // ⚠️ 修复：每个case后必须加break，否则会穿透到下一个case
        // 原bug：缺少break导致DRUID和HIKARICP都会穿透到default抛出异常
        switch (connectionPool) {
            case DRUID:
                log.debug("[DataSource] 创建Druid连接池");
                dataSource = new DruidDataSourceService().getDataSource(properties);
                break;  // ⚠️ 必须有break，否则会穿透
            case HIKARICP:
                log.debug("[DataSource] 创建HikariCP连接池");
                dataSource = new HikariCPDataSourceService().getDataSource(properties);
                break;  // ⚠️ 必须有break，否则会穿透
            default:
                log.error("[DataSource] 不支持的连接池类型: poolType={}", connectionPool);
                throw new BaseException(I18nUtils.getErrorMessage(SqlErrorCode.CONNECTION_POOL_TYPE_NO_MATCH,connectionPool.getName()));
        }
        
        long cost = System.currentTimeMillis() - startTime;
        log.info("[DataSource] 连接池创建成功: poolType={}, cost={}ms", connectionPool, cost);
        
        return dataSource;
    }
}
