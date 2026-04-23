package com.steven.solomon.filter;

import cn.hutool.core.lang.UUID;
import com.steven.solomon.code.BaseCode;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.holder.RequestHeaderHolder;
import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Configuration
public class RequestFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    try {
      ExceptionUtil.requestId.set(UUID.randomUUID().toString());
      RequestHeaderHolder.setTimeZone(request.getHeader(BaseCode.TIMEZONE));
      chain.doFilter(new ContentCachingRequestWrapper(request), response);
    } finally {
      // 清理 ThreadLocal，防止内存泄漏
      ExceptionUtil.requestId.remove();
      RequestHeaderHolder.remove();
    }
  }
}
