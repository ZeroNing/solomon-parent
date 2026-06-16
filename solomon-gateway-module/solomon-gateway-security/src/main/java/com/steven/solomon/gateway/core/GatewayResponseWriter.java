package com.steven.solomon.gateway.core;

import cn.hutool.json.JSONUtil;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.gateway.constant.GatewayHeaders;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 网关统一错误响应写入器。
 *
 * <p>将鉴权/授权/租户校验失败转换为统一的中文 JSON 响应，
 * 字段结构与 {@code BaseExceptionVO} 保持一致（code/message/status），
 * 便于前端统一处理。</p>
 *
 * @author steven
 */
public class GatewayResponseWriter {

    private static final Logger logger = LoggerFactory.getLogger(GatewayResponseWriter.class);

    /**
     * 写入错误响应并终止请求链。
     *
     * <p>设置 HTTP 状态码、写入 JSON 错误体，返回 {@link Mono#empty()} 使过滤器链不再继续。</p>
     *
     * @param exchange 当前请求交换对象
     * @param status   HTTP 状态码
     * @param code     业务错误码（如 TOKEN_REQUIRED）
     * @param message  中文提示信息
     * @return 空 Mono，终止过滤器链
     */
    public Mono<Void> writeError(ServerWebExchange exchange, HttpStatus status, String code, String message) {
        logger.warn("网关拒绝请求: 路径={}, 状态={}, 错误码={}, 提示={}",
                exchange.getRequest().getPath().value(), status.value(), code, message);

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        // 清理可能残留的不可信身份头，避免下游误用
        response.getHeaders().remove(GatewayHeaders.TENANT_CODE);
        response.getHeaders().remove(GatewayHeaders.USER_ID);

        Map<String, Object> body = new LinkedHashMap<>(4);
        body.put(BaseCode.ERROR_CODE, code);
        body.put(BaseCode.MESSAGE, message);
        body.put(BaseCode.HTTP_STATUS, status.value());

        String json = JSONUtil.toJsonStr(body);
        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
