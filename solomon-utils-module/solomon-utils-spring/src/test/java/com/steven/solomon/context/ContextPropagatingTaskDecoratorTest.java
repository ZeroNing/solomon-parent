package com.steven.solomon.context;

import static org.assertj.core.api.Assertions.assertThat;

import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.holder.RequestHeaderHolder;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ContextPropagatingTaskDecoratorTest {

  @AfterEach
  void tearDown() {
    ExceptionUtil.requestId.remove();
    RequestHeaderHolder.remove();
  }

  @Test
  void shouldDecorateTaskWithCapturedContext() {
    ExceptionUtil.requestId.set("request-decorated");
    RequestHeaderHolder.setTenantCode("tenant-decorated");
    ContextPropagatingTaskDecorator decorator = new ContextPropagatingTaskDecorator();
    AtomicReference<String> captured = new AtomicReference<>();

    Runnable decorated = decorator.decorate(() ->
        captured.set(ExceptionUtil.requestId.get() + ":" + RequestHeaderHolder.getTenantCode()));
    ExceptionUtil.requestId.remove();
    RequestHeaderHolder.remove();
    decorated.run();

    assertThat(captured).hasValue("request-decorated:tenant-decorated");
    assertThat(ExceptionUtil.requestId.get()).isNull();
    assertThat(RequestHeaderHolder.getTenantCode()).isEmpty();
  }
}
