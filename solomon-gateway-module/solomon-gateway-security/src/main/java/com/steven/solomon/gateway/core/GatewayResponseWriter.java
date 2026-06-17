package com.steven.solomon.gateway.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.gateway.constant.GatewayHeaders;
import com.steven.solomon.utils.i18n.I18nUtils;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关统一错误响应写入器。
 *
 * <p>将鉴权/授权/租户校验失败转换为与项目 {@code BaseExceptionVO} 结构完全一致的 JSON 响应，
 * 字段为 {@code status}/{@code errorCode}/{@code message}/{@code serverId}/{@code requestId}，
 * 便于前端统一处理。错误提示通过 i18n 解析，根据请求语言环境返回中文或英文文案。</p>
 *
 * @author steven
 */
public class GatewayResponseWriter {

    private static final Logger logger = LoggerFactory.getLogger(GatewayResponseWriter.class);

    /** 服务标识，从 spring.application.id 读取，与 GlobalExceptionHandler 保持一致。 */
    @Value("${spring.application.id:default}")
    private String serverId;

    /**
     * 写入错误响应并终止请求链。
     *
     * <p>响应结构严格对齐项目统一报错格式：</p>
     * <pre>{@code
     * {
     *   "status": 403,
     *   "errorCode": "GATEWAY_ACCESS_DENIED",
     *   "message": "无权访问该接口",
     *   "serverId": "gateway",
     *   "requestId": null
     * }
     * }</pre>
     *
     * @param exchange 当前请求交换对象
     * @param status   HTTP 状态码
     * @param code     业务错误码（同时是 i18n key，如 GATEWAY_TOKEN_REQUIRED）
     * @return 空 Mono，终止过滤器链
     */
    public Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, String code) {
        return writeError(exchange, status, code, (Object[]) null);
    }

    /**
     * 写入错误响应（带 i18n 占位参数）并终止请求链。
     *
     * @param exchange 当前请求交换对象
     * @param status   HTTP 状态码
     * @param code     业务错误码（同时是 i18n key）
     * @param args     i18n 消息占位参数
     * @return 空 Mono，终止过滤器链
     */
    public Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, String code, Object... args) {
        // 通过 i18n 解析错误文案，根据请求语言环境返回中文或英文
        String message = I18nUtils.getErrorMessage(code, args);
        if (StrUtil.isBlank(message)) {
            message = code;
        }

        logger.warn("网关拒绝请求: 路径={}, 状态={}, 错误码={}, 提示={}",
                exchange.getRequest().getPath().value(), status.value(), code, message);

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        // 清理可能残留的不可信身份头，避免下游误用
        response.getHeaders().remove(GatewayHeaders.TENANT_CODE);
        response.getHeaders().remove(GatewayHeaders.USER_ID);

        // 严格对齐项目 BaseExceptionVO 的 JSON 结构
        Map<String, Object> body = new LinkedHashMap<>(5);
        body.put(BaseCode.HTTP_STATUS, status.value());
        body.put(BaseCode.ERROR_CODE, code);
        body.put(BaseCode.MESSAGE, message);
        body.put(BaseCode.SERVER_ID, serverId);
        body.put(BaseCode.REQUEST_ID, exchange.getRequest().getId());

        String json = JSONUtil.toJsonStr(body);
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
