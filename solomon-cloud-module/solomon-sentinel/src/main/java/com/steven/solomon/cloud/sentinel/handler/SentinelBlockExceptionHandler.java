package com.steven.solomon.cloud.sentinel.handler;

import cn.hutool.json.JSONUtil;

import com.alibaba.csp.sentinel.adapter.spring.webflux.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Sentinel 限流熔断统一异常处理器。
 *
 * <p>当请求被 Sentinel 拦截（限流、熔断、降级）时，本处理器返回统一的 JSON 错误响应，
 * 包含中文错误提示和对应的 HTTP 状态码，避免前端收到不可读的异常信息。</p>
 *
 * <p>不同 BlockException 类型对应不同的错误提示：</p>
 * <ul>
 *   <li>{@link FlowException}：请求过于频繁，已被限流（429）。</li>
 *   <li>{@link DegradeException}：服务暂时不可用，已被熔断降级（503）。</li>
 *   <li>其他：服务受限，请稍后重试（429）。</li>
 * </ul>
 *
 * @author steven
 */
public class SentinelBlockExceptionHandler implements BlockRequestHandler {

    private static final Logger logger = LoggerUtils.logger(SentinelBlockExceptionHandler.class);

    @Override
    public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable t) {
        // 根据异常类型确定中文错误提示和 HTTP 状态码
        String message = "服务受限，请稍后重试";
        HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;

        if (t instanceof FlowException) {
            message = "请求过于频繁，已被限流，请稍后重试";
            status = HttpStatus.TOO_MANY_REQUESTS;
        } else if (t instanceof DegradeException) {
            message = "服务暂时不可用，已被熔断降级，请稍后重试";
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }

        logger.warn("Sentinel 拦截请求: 路径={}, 异常类型={}, 提示={}",
                exchange.getRequest().getPath().value(),
                t.getClass().getSimpleName(), message);

        // 构建统一的中文错误响应体
        Map<String, Object> body = new HashMap<>(4);
        body.put("code", status.value());
        body.put("message", message);
        body.put("status", status.value());

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(JSONUtil.toJsonStr(body));
    }
}
