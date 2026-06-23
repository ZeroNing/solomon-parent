package com.steven.solomon.filter;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.context.TenantModeResolver;
import com.steven.solomon.context.TenantRequestBinder;
import com.steven.solomon.context.TenantResourceScope;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.holder.RequestHeader;
import com.steven.solomon.holder.RequestHeaderHolder;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@ConditionalOnClass(WebFilter.class)
@ConditionalOnProperty(prefix = "solomon.webflux.request-filter", name = "enabled",
    havingValue = "true", matchIfMissing = true)
public class ReactiveRequestContextWebFilter implements WebFilter {

  public static final String REACTOR_CONTEXT_KEY =
      ReactiveRequestContextWebFilter.class.getName() + ".snapshot";

  private final List<TenantRequestBinder> tenantRequestBinders;
  private final TenantModeResolver tenantModeResolver;

  public ReactiveRequestContextWebFilter(List<TenantRequestBinder> tenantRequestBinders,
      TenantModeResolver tenantModeResolver) {
    this.tenantRequestBinders = tenantRequestBinders == null
        ? Collections.emptyList() : List.copyOf(tenantRequestBinders);
    this.tenantModeResolver = tenantModeResolver;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    return Mono.defer(() -> {
      String previousRequestId = ExceptionUtil.requestId.get();
      RequestHeader previousHeader = RequestHeaderHolder.snapshot();
      TenantResourceScope tenantResourceScope = null;
      try {
        bind(exchange.getRequest().getHeaders());
        tenantResourceScope =
            TenantResourceScope.open(RequestHeaderHolder.getTenantCode(), tenantRequestBinders);
        RequestHeader capturedHeader = RequestHeaderHolder.snapshot();
        String capturedRequestId = ExceptionUtil.requestId.get();
        TenantResourceScope scopeToClose = tenantResourceScope;
        return chain.filter(exchange)
            .contextWrite(context -> context.put(REACTOR_CONTEXT_KEY,
                new ReactiveRequestContext(capturedRequestId, capturedHeader)))
            .doFinally(signalType -> {
              closeQuietly(scopeToClose);
              restore(previousRequestId, previousHeader);
            });
      } catch (Exception ex) {
        closeQuietly(tenantResourceScope);
        restore(previousRequestId, previousHeader);
        return Mono.error(ex);
      }
    });
  }

  private void bind(HttpHeaders headers) {
    ExceptionUtil.requestId.set(resolveRequestId(headers.getFirst(BaseCode.REQUEST_ID)));
    RequestHeaderHolder.setTimeZone(headers.getFirst(BaseCode.TIMEZONE));
    RequestHeaderHolder.setTenantCode(
        tenantModeResolver.resolve(headers.getFirst(BaseCode.TENANT_CODE)));
    RequestHeaderHolder.setTenantId(headers.getFirst(BaseCode.TENANT_ID));
    RequestHeaderHolder.setTenantName(headers.getFirst(BaseCode.TENANT_NAME));
  }

  private String resolveRequestId(String requestId) {
    if (requestId == null || requestId.isBlank()) {
      return UUID.randomUUID().toString();
    }
    return requestId.trim();
  }

  private void closeQuietly(TenantResourceScope tenantResourceScope) {
    if (tenantResourceScope != null) {
      try {
        tenantResourceScope.close();
      } catch (Exception ignored) {
        // keep reactive cleanup non-disruptive
      }
    }
  }

  private void restore(String requestId, RequestHeader requestHeader) {
    if (requestId == null) {
      ExceptionUtil.requestId.remove();
    } else {
      ExceptionUtil.requestId.set(requestId);
    }
    RequestHeaderHolder.restore(requestHeader);
  }

  public record ReactiveRequestContext(String requestId, RequestHeader requestHeader) {
  }
}
