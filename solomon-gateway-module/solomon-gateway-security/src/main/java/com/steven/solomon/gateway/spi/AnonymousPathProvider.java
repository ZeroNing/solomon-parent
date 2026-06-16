package com.steven.solomon.gateway.spi;

import java.util.List;

/**
 * 匿名路径提供者（SPI 接口，由客户实现）。
 *
 * <p>微服务场景下，下游服务的匿名接口（{@code @RequirePermission(anonymous=true)}）
 * 需要同步到网关，网关据此放行无需 Token 的请求。SDK 默认实现从本地权限扫描结果
 * 收集匿名路径；微服务部署时客户可替换为从权限中心（Nacos/DB）加载的实现。</p>
 *
 * @author steven
 */
public interface AnonymousPathProvider {

    /**
     * 返回无需 Token 即可访问的匿名路径模式列表。
     *
     * <p>路径支持 Ant 风格通配符（如 {@code /api/public/**}）。</p>
     *
     * @return 匿名路径列表；无匿名路径时返回空列表
     */
    List<String> getAnonymousPaths();
}
