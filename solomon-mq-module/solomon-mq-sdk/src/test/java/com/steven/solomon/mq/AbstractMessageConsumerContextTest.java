package com.steven.solomon.mq;

import static org.assertj.core.api.Assertions.assertThat;

import com.steven.solomon.context.TenantModeResolver;
import com.steven.solomon.context.TenantModeProperties;
import com.steven.solomon.context.TenantRequestBinder;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.holder.RequestHeaderHolder;
import com.steven.solomon.mq.model.BaseMq;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AbstractMessageConsumerContextTest {

  @AfterEach
  void tearDown() {
    ExceptionUtil.requestId.remove();
    RequestHeaderHolder.remove();
  }

  @Test
  void shouldBindMessageContextDuringConsumptionAndRestorePreviousContext() throws Exception {
    ExceptionUtil.requestId.set("request-before");
    RequestHeaderHolder.setTenantCode("tenant-before");
    TestConsumer consumer = new TestConsumer();
    String json = "{\"body\":\"hello\",\"tenantCode\":\"tenant-message\",\"msgId\":\"msg-1\"}";

    consumer.consume("topic-a", json.getBytes(StandardCharsets.UTF_8));

    assertThat(consumer.seenRequestId).isEqualTo("msg-1");
    assertThat(consumer.seenTenantCode).isEqualTo("tenant-message");
    assertThat(ExceptionUtil.requestId.get()).isEqualTo("request-before");
    assertThat(RequestHeaderHolder.getTenantCode()).isEqualTo("tenant-before");
  }

  static class TestMq extends BaseMq<String> {
  }

  static class TestConsumer extends AbstractMessageConsumer<String, String, TestMq> {

    private String seenRequestId;
    private String seenTenantCode;

    void consume(String topic, byte[] payload) throws Exception {
      consumeMessage(topic, payload);
    }

    @Override
    protected Class<TestMq> messageModelType() {
      return TestMq.class;
    }

    @Override
    public String handleMessage(String body) {
      seenRequestId = ExceptionUtil.requestId.get();
      seenTenantCode = RequestHeaderHolder.getTenantCode();
      return body;
    }

    @Override
    public void saveLog(String result, Throwable throwable, TestMq model) {
    }

    @Override
    protected TenantModeResolver tenantModeResolver() {
      return new TenantModeResolver(new TenantModeProperties());
    }

    @Override
    protected Collection<TenantRequestBinder> tenantRequestBinders() {
      return Collections.emptyList();
    }
  }
}
