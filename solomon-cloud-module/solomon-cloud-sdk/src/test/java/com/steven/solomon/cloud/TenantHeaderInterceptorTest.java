package com.steven.solomon.cloud;

import static org.assertj.core.api.Assertions.assertThat;

import com.steven.solomon.code.BaseCode;
import com.steven.solomon.exception.ExceptionUtil;
import com.steven.solomon.holder.RequestHeaderHolder;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantHeaderInterceptorTest {

  @AfterEach
  void tearDown() {
    ExceptionUtil.requestId.remove();
    RequestHeaderHolder.remove();
  }

  @Test
  void shouldPropagateRequestContextHeaders() {
    ExceptionUtil.requestId.set("request-feign");
    RequestHeaderHolder.setTimeZone("Asia/Shanghai");
    RequestHeaderHolder.setTenantCode("tenant-feign");
    RequestHeaderHolder.setTenantId("tenant-id");
    RequestHeaderHolder.setTenantName("tenant-name");
    RequestTemplate template = new RequestTemplate();

    new TenantHeaderInterceptor().apply(template);

    assertThat(template.headers().get(BaseCode.REQUEST_ID)).containsExactly("request-feign");
    assertThat(template.headers().get(BaseCode.TIMEZONE)).containsExactly("Asia/Shanghai");
    assertThat(template.headers().get(BaseCode.TENANT_CODE)).containsExactly("tenant-feign");
    assertThat(template.headers().get(BaseCode.TENANT_ID)).containsExactly("tenant-id");
    assertThat(template.headers().get(BaseCode.TENANT_NAME)).containsExactly("tenant-name");
  }
}
