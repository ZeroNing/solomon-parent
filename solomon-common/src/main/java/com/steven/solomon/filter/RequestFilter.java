package com.steven.solomon.filter;

import cn.hutool.core.lang.UUID;
import com.steven.solomon.code.BaseCode;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.holder.RequestHeaderHolder;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Configuration
public class RequestFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    //设置时区值
    ExceptionUtil.requestId.set(UUID.randomUUID().toString());
    RequestHeaderHolder.setTimeZone(request.getHeader(BaseCode.TIMEZONE));
    //往下执行
    chain.doFilter(new ContentCachingRequestWrapper(request), response);
  }
}
