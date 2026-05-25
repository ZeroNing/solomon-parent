package com.steven.solomon.filter;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.holder.RequestHeaderHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * 请求上下文过滤器。
 *
 * <p>每个请求进入时生成 requestId，并把常用请求头写入 ThreadLocal。
 * 请求结束后统一清理 ThreadLocal，防止线程池复用导致请求上下文串用。</p>
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "solomon.web.request-filter", name = "enabled",
    havingValue = "true", matchIfMissing = true)
public class RequestFilter extends OncePerRequestFilter {

  /**
   * 初始化请求上下文并包装请求体缓存。
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    try {
      ExceptionUtil.requestId.set(UUID.randomUUID().toString());
      RequestHeaderHolder.setTimeZone(request.getHeader(BaseCode.TIMEZONE));
      chain.doFilter(wrapRequest(request), response);
    } finally {
      ExceptionUtil.requestId.remove();
      RequestHeaderHolder.remove();
    }
  }

  /**
   * 包装请求对象，使全局异常处理器能够读取已缓存的请求体。
   */
  private HttpServletRequest wrapRequest(HttpServletRequest request) {
    if (request instanceof ContentCachingRequestWrapper) {
      return request;
    }
    return new ContentCachingRequestWrapper(request);
  }
}
