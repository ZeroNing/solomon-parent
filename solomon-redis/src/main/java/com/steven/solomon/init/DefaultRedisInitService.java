package com.steven.solomon.init;

import cn.hutool.core.util.ObjectUtil;

import com.steven.solomon.config.RedisTenantContext;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

import java.time.Duration;

/**
 * 默认Redis租户初始化服务
 * 
 * <h2>功能说明</h2>
 * <p>负责为多租户环境创建和管理Redis连接工厂</p>
 * 
 * <h2>核心流程</h2>
 * <ol>
 *   <li>解析Redis配置（主机、端口、密码、数据库索引）</li>
 *   <li>配置连接池参数（最大连接数、空闲连接数、超时时间等）</li>
 *   <li>创建Lettuce连接工厂</li>
 *   <li>注册到租户上下文</li>
 * </ol>
 * 
 * <h2>连接池配置说明</h2>
 * <ul>
 *   <li><b>maxIdle</b> - 最大空闲连接数，默认0</li>
 *   <li><b>minIdle</b> - 最小空闲连接数，默认0</li>
 *   <li><b>maxActive</b> - 最大活跃连接数，默认8</li>
 *   <li><b>maxWait</b> - 获取连接最大等待时间，默认-1（无限等待）</li>
 * </ul>
 * 
 * @author steven
 * @since 1.0.0
 * @see RedisTenantContext
 * @see LettuceConnectionFactory
 */
public class DefaultRedisInitService extends AbstractTenantResourceInitService<RedisProperties, RedisTenantContext, LettuceConnectionFactory>{
    
    /**
     * 创建Redis连接工厂
     * 
     * <h3>配置步骤</h3>
     * <ol>
     *   <li>创建连接池配置（从properties中读取）</li>
     *   <li>创建Redis单机配置（主机、端口、密码、数据库）</li>
     *   <li>组合创建LettuceConnectionFactory</li>
     *   <li>调用afterPropertiesSet()完成初始化</li>
     * </ol>
     * 
     * <h3>⚠️ 注意事项</h3>
     * <ul>
     *   <li>必须调用afterPropertiesSet()才能使用连接工厂</li>
     *   <li>连接池参数默认值可能导致性能问题，建议显式配置</li>
     * </ul>
     * 
     * @param properties Redis配置
     * @return 配置好的Lettuce连接工厂
     * @throws Throwable 配置错误时抛出
     */
    @Override
    public LettuceConnectionFactory initFactory(RedisProperties properties) throws Throwable {
        log.debug("[Redis] 开始创建连接工厂: host={}:{}, database={}", 
            properties.getHost(), properties.getPort(), properties.getDatabase());
        
        // ========== Step 1: 配置连接池 ==========
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        RedisProperties.Pool pool = properties.getLettuce().getPool();
        
        if (ObjectUtil.isNotEmpty(pool)) {
            // 连接池参数配置，缺省值由 Hutool ObjectUtil 提供。
            genericObjectPoolConfig.setMaxIdle(ObjectUtil.defaultIfNull(pool.getMaxIdle(), 0));
            genericObjectPoolConfig.setMinIdle(ObjectUtil.defaultIfNull(pool.getMinIdle(), 0));
            genericObjectPoolConfig.setMaxTotal(ObjectUtil.defaultIfNull(pool.getMaxActive(), 8));
            genericObjectPoolConfig.setMaxWaitMillis(ObjectUtil.defaultIfNull(pool.getMaxWait().toMillis(), -1L));
            genericObjectPoolConfig.setTimeBetweenEvictionRunsMillis(
                ObjectUtil.defaultIfNull(ObjectUtil.defaultIfNull(pool.getTimeBetweenEvictionRuns(),
                    Duration.ofMillis(60L)).toMillis(), 60L));
            
            log.debug("[Redis] 连接池配置: maxIdle={}, minIdle={}, maxTotal={}, maxWait={}ms", 
                genericObjectPoolConfig.getMaxIdle(), 
                genericObjectPoolConfig.getMinIdle(), 
                genericObjectPoolConfig.getMaxTotal(), 
                genericObjectPoolConfig.getMaxWaitMillis());
        } else {
            log.warn("[Redis] 未配置连接池参数，使用默认值");
        }
        
        // ========== Step 2: 配置Redis单机连接 ==========
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setDatabase(properties.getDatabase());
        redisStandaloneConfiguration.setHostName(properties.getHost());
        redisStandaloneConfiguration.setPort(properties.getPort());
        redisStandaloneConfiguration.setPassword(RedisPassword.of(properties.getPassword()));
        
        log.debug("[Redis] Redis配置: host={}:{}, database={}, password={}", 
            properties.getHost(), properties.getPort(), properties.getDatabase(), 
            ObjectUtil.isNotEmpty(properties.getPassword()) ? "已设置" : "未设置");
        
        // ========== Step 3: 创建Lettuce客户端配置 ==========
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                // 注释掉的配置项（可根据需要启用）：
                // .commandTimeout(ObjectUtil.defaultIfNull(redisProperties.getTimeout(), Duration.ofMillis(60L)))
                // .shutdownTimeout(ObjectUtil.defaultIfNull(redisProperties.getLettuce().getShutdownTimeout(), Duration.ofMillis(100)))
                .poolConfig(genericObjectPoolConfig)
                .build();
        
        // ========== Step 4: 创建连接工厂并初始化 ==========
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfig);
        
        // ⚠️ 必须调用afterPropertiesSet()完成初始化，否则连接工厂无法使用
        factory.afterPropertiesSet();
        
        log.info("[Redis] 连接工厂创建成功: host={}:{}, database={}", 
            properties.getHost(), properties.getPort(), properties.getDatabase());
        
        return factory;
    }
}
