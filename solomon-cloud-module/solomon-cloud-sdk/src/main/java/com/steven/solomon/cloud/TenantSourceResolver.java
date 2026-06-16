package com.steven.solomon.cloud;

/**
 * 租户编码来源解析策略。
 *
 * <p>微服务场景下租户编码有多种来源，本接口将「从哪里获取租户编码」抽象为可插拔策略，
 * 便于在不同部署模式下切换，而不改动消费侧业务代码：</p>
 *
 * <ul>
 *   <li>{@link HeaderTenantSourceResolver}（默认）：从 Gateway 透传的 {@code X-Tenant-Code}
 *       请求头读取。Gateway 完成 JWT 鉴权后，从 Token 解析出租户编码并写入可信头，
 *       下游服务只需信任并读取该头。这是推荐的标准链路。</li>
 *   <li>未来可扩展 {@code TokenTenantSourceResolver}：当下游服务不经过 Gateway 直连调用时，
 *       直接从请求携带的 JWT Token 中解析租户编码（需要业务侧自行实现 JWT 解析逻辑）。</li>
 * </ul>
 *
 * <p>无论哪种来源，最终都交由 {@code TenantModeResolver} 做单/多租户模式校验，
 * 再绑定到 {@code TenantRequestBinder} 完成资源切换，保持下游处理流程统一。</p>
 *
 * @author steven
 */
public interface TenantSourceResolver {

    /**
     * 解析当前请求/消息的租户编码。
     *
     * @return 租户编码；无法解析时返回 {@code null}，由 {@code TenantModeResolver} 决定回退或拒绝
     */
    String resolve();
}
