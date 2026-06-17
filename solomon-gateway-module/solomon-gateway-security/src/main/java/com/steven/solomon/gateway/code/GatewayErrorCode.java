package com.steven.solomon.gateway.code;

/**
 * 网关错误码常量。
 *
 * <p>统一网关鉴权、授权、租户校验失败时的错误码，每个错误码同时也是 i18n key，
 * 在 {@code messages_*.properties} 中配置对应语言的提示文案。</p>
 *
 * @author steven
 */
public final class GatewayErrorCode {

    /** 缺少令牌（401）。 */
    public static final String TOKEN_REQUIRED = "GATEWAY_TOKEN_REQUIRED";

    /** 令牌无效或已过期（401）。 */
    public static final String TOKEN_INVALID = "GATEWAY_TOKEN_INVALID";

    /** 访问被拒绝（403）。 */
    public static final String ACCESS_DENIED = "GATEWAY_ACCESS_DENIED";

    /** 授权校验器未配置（403）。 */
    public static final String ACCESS_VALIDATOR_MISSING = "GATEWAY_ACCESS_VALIDATOR_MISSING";

    /** 授权校验失败（403）。 */
    public static final String ACCESS_VALIDATE_FAILED = "GATEWAY_ACCESS_VALIDATE_FAILED";

    /** 租户无效（403）。 */
    public static final String TENANT_INVALID = "GATEWAY_TENANT_INVALID";

    /** 租户校验器未配置（403）。 */
    public static final String TENANT_VALIDATOR_MISSING = "GATEWAY_TENANT_VALIDATOR_MISSING";

    /** 租户校验失败（403）。 */
    public static final String TENANT_VALIDATE_FAILED = "GATEWAY_TENANT_VALIDATE_FAILED";

    /** 租户编码格式不合法（403）。 */
    public static final String TENANT_CODE_INVALID = "GATEWAY_TENANT_CODE_INVALID";

    private GatewayErrorCode() {
    }
}
