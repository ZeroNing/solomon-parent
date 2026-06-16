package com.steven.solomon.gateway.constant;

import com.steven.solomon.code.BaseCode;

/**
 * 网关 HTTP 请求头常量。
 *
 * <p>网关在鉴权后只会向下游写入这些可信头，并清理客户端伪造的同名头，
 * 保证下游服务拿到的身份信息全部来自网关的权威计算。
 * 头名称复用 {@link BaseCode}，与下游 {@code solomon-common} 的读取逻辑保持一致。</p>
 *
 * @author steven
 */
public final class GatewayHeaders {

    /** 授权头名称，承载 Bearer Token。 */
    public static final String AUTHORIZATION = "Authorization";

    /** Bearer Token 前缀。 */
    public static final String BEARER_PREFIX = BaseCode.HEADER_PREFIX;

    /** 租户编码头，网关从 Token 解析后写入。 */
    public static final String TENANT_CODE = BaseCode.TENANT_CODE;

    /** 用户标识头，网关从 Token 解析后写入。 */
    public static final String USER_ID = BaseCode.USER_ID;

    /** 租户ID头（部分下游按数字ID识别租户）。 */
    public static final String TENANT_ID = BaseCode.TENANT_ID;

    /** 租户名称头。 */
    public static final String TENANT_NAME = BaseCode.TENANT_NAME;

    private GatewayHeaders() {
    }
}
