package com.steven.solomon.context;

import static org.assertj.core.api.Assertions.assertThat;

import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.holder.RequestHeaderHolder;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class RequestContextSnapshotTest {

  @AfterEach
  void tearDown() {
    ExceptionUtil.requestId.remove();
    RequestHeaderHolder.remove();
  }

  @Test
  void shouldPropagateRunnableContextAndRestorePreviousContext() {
    ExceptionUtil.requestId.set("request-1");
    RequestHeaderHolder.setTenantCode("tenant-1");
    RequestContextSnapshot snapshot = RequestContextSnapshot.capture();
    ExceptionUtil.requestId.set("request-previous");
    RequestHeaderHolder.setTenantCode("tenant-previous");
    AtomicReference<String> requestId = new AtomicReference<>();
    AtomicReference<String> tenantCode = new AtomicReference<>();

    snapshot.wrap(() -> {
      requestId.set(ExceptionUtil.requestId.get());
      tenantCode.set(RequestHeaderHolder.getTenantCode());
    }).run();

    assertThat(requestId).hasValue("request-1");
    assertThat(tenantCode).hasValue("tenant-1");
    assertThat(ExceptionUtil.requestId.get()).isEqualTo("request-previous");
    assertThat(RequestHeaderHolder.getTenantCode()).isEqualTo("tenant-previous");
  }

  @Test
  void shouldPropagateCallableAndSupplierContext() throws Exception {
    ExceptionUtil.requestId.set("request-2");
    RequestHeaderHolder.setTenantCode("tenant-2");
    RequestContextSnapshot snapshot = RequestContextSnapshot.capture();
    Callable<String> rawCallable = () ->
        ExceptionUtil.requestId.get() + ":" + RequestHeaderHolder.getTenantCode();
    Callable<String> callable = snapshot.wrap(rawCallable);
    Supplier<String> supplier = snapshot.wrap((Supplier<String>) () ->
        ExceptionUtil.requestId.get() + ":" + RequestHeaderHolder.getTenantCode());

    assertThat(callable.call()).isEqualTo("request-2:tenant-2");
    assertThat(supplier.get()).isEqualTo("request-2:tenant-2");
  }
}
