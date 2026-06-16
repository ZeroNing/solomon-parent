package com.steven.solomon.security.spi;

import com.steven.solomon.security.core.TokenClaims;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 授权校验器（SPI 接口，由客户实现）。
 *
 * <p>服务在完成 Token 鉴权（确认身份合法）后，调用本接口校验当前用户是否有权访问
 * 目标接口。客户实现内可查询角色、权限等业务数据源。</p>
 *
 * <p><b>默认拒绝原则</b>：客户未声明本接口 Bean 时，所有受保护接口一律返回 403。</p>
 *
 * @author steven
 */
public interface AccessValidator {

    /**
     * 校验用户是否有权访问当前请求的接口。
     *
     * @param claims  从 Token 解析出的身份声明
     * @param request 当前 HTTP 请求，可获取路径、方法等
     * @return true 表示允许访问；false 表示拒绝（403）
     */
    boolean validate(TokenClaims claims, HttpServletRequest request);
}
