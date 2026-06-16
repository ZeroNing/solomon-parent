package com.steven.solomon.filter;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.context.TenantModeResolver;
import com.steven.solomon.context.TenantRequestBinder;
import com.steven.solomon.context.TenantResourceScope;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.holder.RequestHeaderHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
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

  private final List<TenantRequestBinder> tenantRequestBinders;
  private final TenantModeResolver tenantModeResolver;

  public RequestFilter(List<TenantRequestBinder> tenantRequestBinders,
      TenantModeResolver tenantModeResolver) {
    this.tenantRequestBinders = tenantRequestBinders == null
        ? Collections.emptyList() : List.copyOf(tenantRequestBinders);
    this.tenantModeResolver = tenantModeResolver;
  }

  /**
   * 初始化请求上下文并包装请求体缓存。
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    TenantResourceScope tenantResourceScope = null;
    try {
      ExceptionUtil.requestId.set(UUID.randomUUID().toString());
      RequestHeaderHolder.setTimeZone(request.getHeader(BaseCode.TIMEZONE));
      RequestHeaderHolder.setTenantCode(
          tenantModeResolver.resolve(request.getHeader(BaseCode.TENANT_CODE)));
      RequestHeaderHolder.setTenantId(request.getHeader(BaseCode.TENANT_ID));
      RequestHeaderHolder.setTenantName(request.getHeader(BaseCode.TENANT_NAME));
      tenantResourceScope =
          TenantResourceScope.open(RequestHeaderHolder.getTenantCode(), tenantRequestBinders);
      chain.doFilter(wrapRequest(request), response);
    } catch (ServletException | IOException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ServletException(ex);
    } finally {
      if (tenantResourceScope != null) {
        tenantResourceScope.close();
      }
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
