package com.steven.solomon.cloud.sentinel.handler;

import cn.hutool.json.JSONUtil;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;

/**
 * Sentinel 限流熔断统一异常处理器。
 *
 * <p>当请求被 Sentinel 拦截（限流、熔断、降级）时，由本处理器生成统一的中文 JSON 错误响应体，
 * 供网关适配器或业务拦截器写入 HTTP 响应。</p>
 *
 * <p>不同 BlockException 类型对应不同的错误提示：</p>
 * <ul>
 *   <li>{@link FlowException}：请求过于频繁，已被限流。</li>
 *   <li>{@link DegradeException}：服务暂时不可用，已被熔断降级。</li>
 *   <li>其他：服务受限，请稍后重试。</li>
 * </ul>
 *
 * @author steven
 */
public class SentinelBlockExceptionHandler {

    private static final Logger logger = LoggerUtils.logger(SentinelBlockExceptionHandler.class);

    /**
     * 根据 Sentinel 拦截异常生成统一的中文错误响应体（JSON 字符串）。
     *
     * @param path      请求路径（日志用）
     * @param throwable Sentinel 拦截异常
     * @return 包含 errorCode/message/status 的 JSON 字符串
     */
    public String handleBlockException(String path, Throwable throwable) {
        int statusCode = 429;
        String message = "服务受限，请稍后重试";

        if (throwable instanceof FlowException) {
            message = "请求过于频繁，已被限流，请稍后重试";
            statusCode = 429;
        } else if (throwable instanceof DegradeException) {
            message = "服务暂时不可用，已被熔断降级，请稍后重试";
            statusCode = 503;
        }

        logger.warn("Sentinel 拦截请求: 路径={}, 异常类型={}, 状态码={}", path,
                throwable.getClass().getSimpleName(), statusCode);

        Map<String, Object> body = new HashMap<>(4);
        body.put("errorCode", "SENTINEL_BLOCK");
        body.put("message", message);
        body.put("status", statusCode);
        return JSONUtil.toJsonStr(body);
    }
}
