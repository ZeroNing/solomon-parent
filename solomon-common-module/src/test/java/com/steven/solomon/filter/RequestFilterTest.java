package com.steven.solomon.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.context.TenantModeProperties;
import com.steven.solomon.context.TenantModeResolver;
import com.steven.solomon.context.TenantRequestBinder;
import com.steven.solomon.holder.RequestHeaderHolder;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestFilterTest {

  @Test
  void bindAndClearTenantResources() throws Exception {
    AtomicBoolean cleared = new AtomicBoolean();
    TenantRequestBinder binder = new TenantRequestBinder() {
      @Override
      public void bind(String tenantCode) {
        assertEquals("tenant-1", tenantCode);
      }

      @Override
      public void clear() {
        cleared.set(true);
      }
    };
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(BaseCode.TENANT_CODE, "tenant-1");

    new RequestFilter(List.of(binder), tenantModeResolver()).doFilter(request,
        new MockHttpServletResponse(),
        (currentRequest, response) ->
            assertEquals("tenant-1", RequestHeaderHolder.getTenantCode()));

    assertTrue(cleared.get());
    assertEquals("", RequestHeaderHolder.getTenantCode());
  }

  @Test
  void clearTenantResourceWhenBindingFails() {
    AtomicBoolean cleared = new AtomicBoolean();
    TenantRequestBinder binder = new TenantRequestBinder() {
      @Override
      public void bind(String tenantCode) {
        throw new IllegalStateException("bind failed");
      }

      @Override
      public void clear() {
        cleared.set(true);
      }
    };
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(BaseCode.TENANT_CODE, "tenant-1");

    assertThrows(Exception.class,
        () -> new RequestFilter(List.of(binder), tenantModeResolver()).doFilter(request,
            new MockHttpServletResponse(), (currentRequest, response) -> {
            }));

    assertTrue(cleared.get());
    assertEquals("", RequestHeaderHolder.getTenantCode());
  }

  @Test
  void useDefaultTenantWhenHeaderIsMissing() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();

    new RequestFilter(List.of(), tenantModeResolver()).doFilter(request,
        new MockHttpServletResponse(), (currentRequest, response) ->
            assertEquals("default", RequestHeaderHolder.getTenantCode()));

    assertEquals("", RequestHeaderHolder.getTenantCode());
  }

  private TenantModeResolver tenantModeResolver() {
    return new TenantModeResolver(new TenantModeProperties());
  }
}
