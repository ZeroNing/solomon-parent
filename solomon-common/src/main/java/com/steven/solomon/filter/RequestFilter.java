package com.steven.solomon.filter;

import cn.hutool.core.lang.UUID;
import com.steven.solomon.aspect.controller.ControllerAspect;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.json.JackJsonUtils;
import com.steven.solomon.utils.logger.LoggerUtils;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration(proxyBeanMethods = false)
public class RequestFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerUtils.logger(RequestFilter.class);

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    String uuid = UUID.randomUUID().toString();
    ExceptionUtil.requestId.set(uuid);

    logger.info("===========================================");
    logger.info("请求id:{}",uuid);
    logger.info("请求url:{}",request.getRequestURI());
    logger.info("请求参数:{}", JackJsonUtils.formatJsonByFilter(request.getParameterMap()));
    logger.info("===========================================");
    filterChain.doFilter(request, response);

  }
}
