package com.steven.solomon.gateway.filter;

import cn.hutool.core.util.StrUtil;

import com.steven.solomon.gateway.core.TokenClaims;
import com.steven.solomon.gateway.gray.GrayReleaseSelector;
import com.steven.solomon.gateway.properties.GatewayGrayProperties;
import com.steven.solomon.utils.logger.LoggerUtils;
import org.slf4j.Logger;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 灰度发布全局过滤器。
 *
 * <p>在安全过滤器之后执行，按配置决定当前请求走稳定版还是候选版：</p>
 * <ul>
 *   <li>灰度未开启：始终删除客户端传入的灰度版本头，防止客户端自行选择版本。</li>
 *   <li>灰度已开启：按 {@link GrayReleaseSelector} 稳定分桶，写入可信版本头，
 *       下游负载均衡或路由规则据此选择实例。</li>
 * </ul>
 *
 * @author steven
 */
public class GrayReleaseFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerUtils.logger(GrayReleaseFilter.class);

    /** 过滤器优先级，在安全过滤器之后执行。 */
    public static final int ORDER = GatewaySecurityFilter.ORDER + 10;

    /** exchange attribute 中存储灰度版本的键。 */
    public static final String VERSION_ATTRIBUTE = "gateway.gray.version";

    private final GatewayGrayProperties properties;
    private final GrayReleaseSelector selector;

    public GrayReleaseFilter(GatewayGrayProperties properties, GrayReleaseSelector selector) {
        this.properties = properties;
        this.selector = selector;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        // 始终删除客户端传入的灰度头，防止伪造版本
        ServerHttpRequest.Builder builder = exchange.getRequest().mutate()
                .headers(headers -> headers.remove(properties.getHeaderName()));

        if (!selector.isEnabled()) {
            // 灰度未开启，清理后直接放行
            return chain.filter(exchange.mutate().request(builder.build()).build());
        }

        // 从安全过滤器保存的声明中获取身份信息
        TokenClaims claims = exchange.getAttribute(GatewaySecurityFilter.CLAIMS_ATTRIBUTE);
        String version = selector.selectVersion(claims, path);

        // 写入可信灰度版本头
        ServerHttpRequest mutated = builder
                .header(properties.getHeaderName(), version)
                .build();
        exchange.getAttributes().put(VERSION_ATTRIBUTE, version);
        logger.debug("灰度版本选择: 路径={}, 版本={}", path, version);
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
