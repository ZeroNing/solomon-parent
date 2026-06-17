package com.steven.solomon.security.core.spi;

import java.util.List;

/**
 * 匿名路径提供者（SPI 接口，由客户实现）。
 *
 * <p>返回无需 Token 即可访问的路径。SDK 默认从权限扫描结果收集匿名路径，
 * 客户可替换为从配置中心加载的实现。</p>
 *
 * @author steven
 */
public interface AnonymousPathProvider {

    /**
     * 返回无需 Token 的匿名路径模式列表。
     *
     * @return 匿名路径列表；无匿名路径时返回空列表
     */
    List<String> getAnonymousPaths();
}
