package com.steven.solomon.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.context.TenantModeProperties;
import com.steven.solomon.context.TenantModeResolver;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.holder.RequestHeaderHolder;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

class ReactiveRequestContextWebFilterTest {

  @Test
  void bindContextFromHeadersAndClearAfterCompletion() {
    ReactiveRequestContextWebFilter filter =
        new ReactiveRequestContextWebFilter(List.of(), new TenantModeResolver(new TenantModeProperties()));
    MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/")
        .header(BaseCode.REQUEST_ID, "request-reactive")
        .header(BaseCode.TIMEZONE, "Asia/Shanghai")
        .header(BaseCode.TENANT_CODE, "tenant-reactive")
        .header(BaseCode.TENANT_ID, "tenant-id")
        .header(BaseCode.TENANT_NAME, "tenant-name"));
    AtomicReference<String> captured = new AtomicReference<>();

    filter.filter(exchange, current -> {
      captured.set(ExceptionUtil.requestId.get() + ":" + RequestHeaderHolder.getTenantCode()
          + ":" + RequestHeaderHolder.getTenantId() + ":" + RequestHeaderHolder.getTenantName());
      return Mono.empty();
    }).block();

    assertEquals("request-reactive:tenant-reactive:tenant-id:tenant-name", captured.get());
    assertEquals(null, ExceptionUtil.requestId.get());
    assertEquals("", RequestHeaderHolder.getTenantCode());
  }
}
